package com.rockthejvm.akka.cassandra.services

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.rockthejvm.akka.cassandra.services.PersistentBankAccount._

import java.util.UUID

object Bank {

  // Events
  sealed trait Event
  case class BankAccountCreated(id: String, bankAccount: ActorRef[Command]) extends Event

  // State
  case class State(accounts: Map[String, ActorRef[Command]])

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("bank"),
      emptyState = State(Map.empty),
      commandHandler = (state, cmd) => commandHandler(state, cmd, ctx),
      eventHandler = eventHandler
    ).receiveSignal {
      case (state, RecoveryCompleted) =>
        state.accounts.foreach { case (id, _) =>
          ctx.spawn(PersistentBankAccount(id), id)
        }
    }
  }

  val commandHandler: (State, Command, ActorContext[Command]) => Effect[Event, State] = {
    (state, command, ctx) =>
      command match {
        case createCmd @ CreateBankAccount(_, _, _, _) =>
          val id             = UUID.randomUUID().toString
          val newBankAccount = ctx.spawn(PersistentBankAccount(id), id)
          Effect
            .persist(BankAccountCreated(id, newBankAccount))
            .thenReply(newBankAccount)(_ => createCmd)
        case updateCmd @ UpdateBalance(id, _, _, _) =>
          state.accounts.get(id) match {
            case Some(bankAccount) =>
              Effect.none.thenReply(bankAccount)(_ => updateCmd)
            case None => ???
            // TODO: Reply with some error
          }
        case getCmd @ GetBankAccount(id, replyTo) =>
          state.accounts.get(id) match {
            case Some(bankAccount) =>
              Effect.none.thenReply(bankAccount)(_ => getCmd)
            case None =>
              Effect.none.thenReply(replyTo)(_ => GetBankAccountResponse(None))
          }
      }
  }

  val eventHandler: (State, Event) => State = { (state, event) =>
    event match {
      case BankAccountCreated(id, bankAccount) =>
        state.copy(accounts = state.accounts + (id -> bankAccount))
    }
  }
}
