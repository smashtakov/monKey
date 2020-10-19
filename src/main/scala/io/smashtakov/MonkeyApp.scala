package io.smashtakov

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.util.Failure
import scala.util.Success


object MonkeyApp extends App with LazyLogging {


  val configuration = Configuration(ConfigFactory.load())

  val rootBehavior: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val transactionManagerActor = context.spawn(TransactionManager(), "TransactionManagerActor")
    context.watch(transactionManagerActor)

    val routesConfig = configuration.routes
    val routes = new TransactionRoutes(transactionManagerActor, routesConfig)(context.system)
    startHttpServer(routes.transactionRoutes)(context.system)

    Behaviors.empty
  }
  val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "MonKeyHttpServer")




  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {

    import system.executionContext

    val host = configuration.httpServer.host
    val port = configuration.httpServer.port

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


}
