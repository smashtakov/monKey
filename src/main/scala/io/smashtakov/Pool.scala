package io.smashtakov

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import io.smashtakov.Model.{Categories, Category, Transaction, Transactions}

object Pool {

  trait Command
  sealed trait CategoryCommand extends Command
  sealed trait TransactionCommand extends Command


  final case class GetCategories(replyTo: ActorRef[Categories]) extends CategoryCommand
  final case class GetCategory(name: String, replyTo: ActorRef[GetCategoryResponse]) extends CategoryCommand
  final case class CreateCategory(category: Category, replyTo: ActorRef[CategoryActionPerformed]) extends CategoryCommand

  final case class GetCategoryResponse(maybeCategory: Option[Category])
  final case class CategoryActionPerformed(description: String)

  final case class GetTransactions(replyTo: ActorRef[Transactions]) extends TransactionCommand
  final case class CreateTransaction(transaction: Transaction, replyTo: ActorRef[TransactionActionPerformed]) extends TransactionCommand
  final case class GetTransaction(id: String, replyTo: ActorRef[GetTransactionResponse]) extends TransactionCommand
  final case class DeleteTransaction(id: String, replyTo: ActorRef[TransactionActionPerformed]) extends TransactionCommand

  final case class GetTransactionResponse(maybeTransaction: Option[Transaction])
  final case class TransactionActionPerformed(description: String)

  def apply(): Behavior[Command] = pool(Set.empty, Set.empty)

  private def pool(categories: Set[Category], transactions: Set[Transaction]): Behavior[Command] = {
    Behaviors.receiveMessage {
      /** transactions */
      case GetTransactions(replyTo) =>
        replyTo ! Transactions(transactions.toSeq)
        Behaviors.same
      case CreateTransaction(transaction, replyTo) =>
        replyTo ! TransactionActionPerformed(s"Transaction ${transaction.value}, category ${transaction.categoryName} created.")
        val updatedTransactions = transactions + transaction
        pool(categories, updatedTransactions)
      case GetTransaction(id, replyTo) =>
        replyTo ! GetTransactionResponse(transactions.find(_.id == id))
        Behaviors.same
      case DeleteTransaction(id, replyTo) =>
        replyTo ! TransactionActionPerformed(s"Transaction $id deleted.")
        val updatedTransactions = transactions.filterNot(_.id == id)
        pool(categories, updatedTransactions)
      /** categories */
      case GetCategories(replyTo) =>
        replyTo ! Categories(categories.toSeq)
        Behaviors.same
      case CreateCategory(category, replyTo) =>
        replyTo ! CategoryActionPerformed(s"Category ${category.name} created.")
        val updatedCategories = categories + category
        pool(updatedCategories, transactions)
      case GetCategory(name, replyTo) =>
        replyTo ! GetCategoryResponse(categories.find(_.name == name))
        Behaviors.same
    }
  }

}
