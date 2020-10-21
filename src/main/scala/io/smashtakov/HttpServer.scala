package io.smashtakov

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import io.smashtakov.Model.{Categories, Category, Transaction, Transactions}
import io.smashtakov.Pool._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object HttpServer {

  def createPath(path: String): PathMatcher[Unit] = {
    path.split("/").toList.filter(_.nonEmpty) match {
      case Nil => PathMatcher(Slash)
      case head :: tail =>
        tail.foldLeft(PathMatcher(head))((prev, next) => {
          prev / PathMatcher(next)
        })
    }
  }


}


class HttpServer(pool: ActorRef[Command])(implicit val system: ActorSystem[_]) extends LazyLogging {

  import HttpServer._
  import JsonFormats._

  val config: Configuration = Configuration(ConfigFactory.load)
  val routesConfiguration: RoutesConfiguration = config.routes
  val httpServerConfiguration: HttpServerConfiguration = config.httpServer

  private implicit val timeout: Timeout = Timeout.create(routesConfiguration.timeout)


  def startHttpServer(implicit system: ActorSystem[_]): Unit = {

    import system.executionContext

    val host = httpServerConfiguration.host
    val port = httpServerConfiguration.port

    val futureBinding = Http().newServerAt(host, port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }



  val transactionRoutes: Route =
    pathEnd {
      get {
        complete(getTransactions)
      } ~
        post {
          entity(as[Transaction]) { transaction =>
            onSuccess(createTransaction(transaction)) { performed =>
              complete((StatusCodes.Created, performed))
            }
          }
        }
    } ~
      path(Segment) { id =>
        get {
          rejectEmptyResponse {
            onSuccess(getTransaction(id)) { response =>
              complete(response.maybeTransaction)
            }
          }
        } ~
          delete {
            onSuccess(deleteTransaction(id)) { performed =>
              complete((StatusCodes.OK, performed))
            }
          }
      }



  val categoryRoutes: Route =
    pathEnd {
      get {
        complete(getCategories)
      } ~
        post {
          entity(as[Category]) { category =>
            onSuccess(createCategory(category)) { performed =>
              complete((StatusCodes.Created, performed))
            }
          }
        }
    } ~
      path(Segment) { name =>
        rejectEmptyResponse {
          onSuccess(getCategory(name)) { response =>
            complete(response.maybeCategory)
          }
        }
      }


  val routes: Route =
    pathPrefix(createPath(routesConfiguration.transactionsPath))(transactionRoutes) ~
      pathPrefix(createPath(routesConfiguration.categoriesPath))(categoryRoutes)


  def getTransactions: Future[Transactions] =
    pool.ask(GetTransactions)
  def getTransaction(id: String): Future[GetTransactionResponse] =
    pool.ask(GetTransaction(id, _))
  def createTransaction(transaction: Transaction): Future[TransactionActionPerformed] =
    pool.ask(CreateTransaction(transaction, _))
  def deleteTransaction(id: String): Future[TransactionActionPerformed] =
    pool.ask(DeleteTransaction(id, _))

  def getCategories: Future[Categories] =
    pool.ask(GetCategories)
  def getCategory(name: String): Future[GetCategoryResponse] =
    pool.ask(GetCategory(name, _))
  def createCategory(category: Category): Future[CategoryActionPerformed] =
    pool.ask(CreateCategory(category, _))


}
