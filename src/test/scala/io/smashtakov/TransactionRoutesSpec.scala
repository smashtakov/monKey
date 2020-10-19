package io.smashtakov

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import io.smashtakov.Model.Transaction
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}


class TransactionRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  lazy val testKit: ActorTestKit = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing] = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  val transactionManager: ActorRef[TransactionManager.Command] = testKit.spawn(TransactionManager())
  private val routesConfiguration: RoutesConfiguration = RoutesConfiguration(ConfigFactory.load.getConfig("routes"))
  lazy val routes: Route = new TransactionRoutes(transactionManager, routesConfiguration).transactionRoutes

  import JsonFormats._

  "TransactionRoutes" should {
    "return no transactions if no present (GET /transactions)" in {
      val request = HttpRequest(uri = "/transactions")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"transactions":[]}""")
      }
    }

    "be able to add transactions (POST /transactions)" in {
      val transaction = Transaction("123abc", 42.0, "apple")
      val transactionEntity = Marshal(transaction).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/transactions").withEntity(transactionEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Transaction 123abc created."}""")
      }
    }

    "be able to remove transactions (DELETE /transactions)" in {
      val request = Delete(uri = "/transactions/123abc")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"Transaction 123abc deleted."}""")
      }
    }
  }

}
