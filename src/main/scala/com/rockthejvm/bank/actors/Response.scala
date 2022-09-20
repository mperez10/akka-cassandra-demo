package com.rockthejvm.bank.actors

import com.rockthejvm.bank.actors.persistentbankaccount.BankAccount

import scala.util.Try

// responses
sealed trait Response
case class BankAccountCreatedResponse(id: String) extends Response
case class BankAccountBalanceUpdatedResponse(maybeBankAccount: Try[BankAccount]) extends Response
case class GetBankAccountResponse(maybeBankAccount: Option[BankAccount]) extends Response