package io.smashtakov

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.smashtakov.Model.{Transaction, Transactions}
import io.smashtakov.TransactionManager.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val transactionFormat: RootJsonFormat[Transaction] = jsonFormat(
    Transaction,
    "id",
    "value",
    "comment")

  implicit val transactionsFormat: RootJsonFormat[Transactions] = jsonFormat(
    Transactions,
    "transactions")

  implicit val actionPerformedFormat: RootJsonFormat[ActionPerformed] = jsonFormat(
    ActionPerformed,
    "description")

}

