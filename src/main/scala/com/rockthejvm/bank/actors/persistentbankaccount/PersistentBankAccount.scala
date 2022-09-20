package com.rockthejvm.bank.actors.persistentbankaccount

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.rockthejvm.bank.actors.{BankAccountBalanceUpdatedResponse, BankAccountCreatedResponse, Command, CreateBankAccount, GetBankAccount, GetBankAccountResponse, UpdateBalance}

import scala.util.{Failure, Success}

// a single bank account
object PersistentBankAccount {
  /*
     - fault tolerance
     - auditing
   */

  // command handler = message handler => persist an event
  // event handler => update state
  // state

  val commandHandler: (BankAccount, Command) => Effect[Event, BankAccount] = (state, command) =>
    command match {
      case CreateBankAccount(user, currency, initialBalance, bank) =>
        val id = state.id
        /*
          - bank creates me
          - bank sends me CreateBankAccount
          - I persist BankAccountCreated
          - I update my state
          - reply back to bank with the BankAccountCreatedResponse
          - (the bank surfaces the response to the HTTP server)
         */
        Effect
          .persist(BankAccountCreated(BankAccount(id, user, currency, initialBalance))) // persisted into Cassandra
          .thenReply(bank)(_ => BankAccountCreatedResponse(id))
      case UpdateBalance(_, _, amount, bank) =>
        val newBalance = state.balance + amount
        // check here for withdrawal
        if (newBalance < 0) // illegal
          Effect.reply(bank)(BankAccountBalanceUpdatedResponse(Failure(new RuntimeException("Cannot withdraw more than available"))))
        else
          Effect
            .persist(BalanceUpdated(amount))
            .thenReply(bank)(newState => BankAccountBalanceUpdatedResponse(Success(newState)))
      case GetBankAccount(_, bank) =>
        Effect.reply(bank)(GetBankAccountResponse(Some(state)))
    }

  val eventHandler: (BankAccount, Event) => BankAccount = (state, event) =>
    event match {
      case BankAccountCreated(bankAccount) =>
        bankAccount
      case BalanceUpdated(amount) =>
        state.copy(balance = state.balance + amount)
    }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = BankAccount(id, "", "", 0.0), // unused
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
}
