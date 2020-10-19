package io.smashtakov

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.smashtakov.Model.{Transaction, Transactions}


object TransactionManager {

  sealed trait Command
  final case class GetTransactions(replyTo: ActorRef[Transactions]) extends Command
  final case class CreateTransaction(transaction: Transaction, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetTransaction(id: String, replyTo: ActorRef[GetTransactionResponse]) extends Command
  final case class DeleteTransaction(id: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetTransactionResponse(maybeTransaction: Option[Transaction])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = manage(Set.empty)

  private def manage(transactions: Set[Transaction]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetTransactions(replyTo) =>
        replyTo ! Transactions(transactions.toSeq)
        Behaviors.same
      case CreateTransaction(transaction, replyTo) =>
        replyTo ! ActionPerformed(s"Transaction ${transaction.id} created.")
        manage(transactions + transaction)
      case GetTransaction(id, replyTo) =>
        replyTo ! GetTransactionResponse(transactions.find(_.id == id))
        Behaviors.same
      case DeleteTransaction(id, replyTo) =>
        replyTo ! ActionPerformed(s"Transaction $id deleted.")
        manage(transactions.filterNot(_.id == id))
    }
}

