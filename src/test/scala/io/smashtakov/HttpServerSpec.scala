package io.smashtakov

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.smashtakov.Model.Transaction
import io.smashtakov.Pool.Command
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}


class HttpServerSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  val poolActor: ActorRef[Command] = testKit.spawn(Pool())
  lazy val routes: Route = new HttpServer(poolActor).routes

  import JsonFormats._

  "HttpServer" should {
    "return no transactions if no present (GET /transactions)" in {
      val request = HttpRequest(uri = "/transactions")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"transactions":[]}""")
      }
    }

    "be able to add transactions (POST /transactions)" in {
      val transaction = Transaction("1t", 42.0, "20201020", expense = true)
      val transactionEntity = Marshal(transaction).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/transactions").withEntity(transactionEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Transaction 42.0, category Some(Unknown) created."}""")
      }
    }

    "be able to remove transactions (DELETE /transactions)" in {
      val request = Delete(uri = "/transactions/1t")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Transaction 1t deleted."}""")
      }
    }
  }

}
