package io.smashtakov

object Model {


  final case class Transaction(
                                id: String,
                                value:        Double,
                                date:         String,
                                expense:      Boolean,
                                categoryName: Option[String] = Some("Unknown"),
                                comment:      Option[String] = None)

  final case class Transactions(transactions: Seq[Transaction])

  final case class Category(
                             id:       String,
                             name:     String,
                             expense:  Boolean,
                             income:   Boolean)

  final case class Categories(categories: Seq[Category])


}
