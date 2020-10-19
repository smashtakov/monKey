package io.smashtakov

object Model {

  final case class Transaction(id: String, value: Double, comment: String)
  final case class Transactions(transactions: Seq[Transaction])

}
