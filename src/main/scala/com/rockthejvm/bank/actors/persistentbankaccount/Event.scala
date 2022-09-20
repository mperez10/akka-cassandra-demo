package com.rockthejvm.bank.actors.persistentbankaccount

// events = to persist to Cassandra
trait Event
case class BankAccountCreated(bankAccount: BankAccount) extends Event
case class BalanceUpdated(amount: Double) extends Event