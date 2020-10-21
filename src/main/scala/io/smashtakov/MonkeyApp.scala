package io.smashtakov

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.typesafe.scalalogging.LazyLogging


object MonkeyApp extends App with LazyLogging {


  val rootBehavior: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
    val poolActor = context.spawn(Pool(), "PoolControllerActor")
    context.watch(poolActor)

    val httpServer = new HttpServer(poolActor)(context.system)
    httpServer.startHttpServer(context.system)

    Behaviors.empty
  }
  val system: ActorSystem[Nothing] = ActorSystem[Nothing](rootBehavior, "MonKeyHttpServer")


}
