package io.smashtakov

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{PathMatcher, Route}

import scala.concurrent.Future
import io.smashtakov.TransactionManager._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.typesafe.config.Config
import io.smashtakov.Model.{Transaction, Transactions}

class TransactionRoutes(transactionManager: ActorRef[TransactionManager.Command],
                        config: RoutesConfiguration)(implicit val system: ActorSystem[_]) {

  import JsonFormats._

  private implicit val timeout: Timeout = Timeout.create(config.timeout)

  def getTransactions: Future[Transactions] =
    transactionManager.ask(GetTransactions)
  def getTransaction(id: String): Future[GetTransactionResponse] =
    transactionManager.ask(GetTransaction(id, _))
  def createTransaction(transaction: Transaction): Future[ActionPerformed] =
    transactionManager.ask(CreateTransaction(transaction, _))
  def deleteTransaction(id: String): Future[ActionPerformed] =
    transactionManager.ask(DeleteTransaction(id, _))


  def createPath(path: String): PathMatcher[Unit] = {
    path.split("/").toList.filter(_.nonEmpty) match {
      case Nil => PathMatcher(Slash)
      case head :: tail =>
        tail.foldLeft(PathMatcher(head))((prev, next) => {
          prev / PathMatcher(next)
        })
    }
  }

  val transactionRoutes: Route =
    pathPrefix(createPath(config.path)) {
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
    }
}
