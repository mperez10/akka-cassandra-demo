package com.rockthejvm.bank.http.models

import akka.actor.typed.ActorRef
import cats.implicits.catsSyntaxTuple3Semigroupal
import com.rockthejvm.bank.actors.PersistentBankAccount.Command.CreateBankAccount
import com.rockthejvm.bank.actors.PersistentBankAccount.{Command, Response}
import com.rockthejvm.bank.http.validations.Validation.{ValidationResult, Validator, validateMinimum, validateMinimumAbs, validateRequired}

case class BankAccountCreationRequest(user: String, currency: String, balance: Double) {
  def toCommand(replyTo: ActorRef[Response]): Command = CreateBankAccount(user, currency, balance, replyTo)
}

object BankAccountCreationRequest {
  implicit val validator: Validator[BankAccountCreationRequest] = new Validator[BankAccountCreationRequest] {
    override def validate(request: BankAccountCreationRequest): ValidationResult[BankAccountCreationRequest] = {
      val userValidation = validateRequired(request.user, "user")
      val currencyValidation = validateRequired(request.currency, "currency")
      val balanceValidation = validateMinimum(request.balance, 0, "balance")
        .combine(validateMinimumAbs(request.balance, 0.01, "balance"))

      (userValidation, currencyValidation, balanceValidation).mapN(BankAccountCreationRequest.apply)
    }
  }
}