package io.smashtakov

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import io.smashtakov.Model.{Categories, Category, Transaction, Transactions}
import io.smashtakov.Pool.{CategoryActionPerformed, TransactionActionPerformed}

object JsonFormats extends DefaultJsonProtocol with SprayJsonSupport {

  /** transactions JSON */
  implicit val transactionFormat: RootJsonFormat[Transaction] = jsonFormat(
    Transaction,
    "id",
    "value",
    "date",
    "expense",
    "categoryName",
    "comment")

  implicit val transactionsFormat: RootJsonFormat[Transactions] = jsonFormat(
    Transactions,
    "transactions")

  implicit val transactionActionPerformedFormat: RootJsonFormat[TransactionActionPerformed] = jsonFormat(
    TransactionActionPerformed,
    "description")


  /** categories JSON */
  implicit val categoryFormat: RootJsonFormat[Category] = jsonFormat(
    Category,
    "id",
    "name",
    "expense",
    "income")


  implicit val categoriesFormat: RootJsonFormat[Categories] = jsonFormat(
    Categories,
    "categories")

  implicit val categoryActionPerformedFormat: RootJsonFormat[CategoryActionPerformed] = jsonFormat(
    CategoryActionPerformed,
    "description")

}

