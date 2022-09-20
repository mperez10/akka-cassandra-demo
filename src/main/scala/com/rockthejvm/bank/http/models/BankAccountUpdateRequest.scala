package com.rockthejvm.bank.http.models

import akka.actor.typed.ActorRef
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.rockthejvm.bank.actors.{Command, Response, UpdateBalance}
import com.rockthejvm.bank.actors.persistentbankaccount._
import com.rockthejvm.bank.http.validations.Validation.{ValidationResult, Validator, validateMinimumAbs, validateRequired}

case class BankAccountUpdateRequest(currency: String, amount: Double) {
  def toCommand(id: String, replyTo: ActorRef[Response]): Command = UpdateBalance(id, currency, amount, replyTo)
}

object BankAccountUpdateRequest {
  implicit val validator: Validator[BankAccountUpdateRequest] = new Validator[BankAccountUpdateRequest] {
    override def validate(request: BankAccountUpdateRequest): ValidationResult[BankAccountUpdateRequest] = {
      val currencyValidation = validateRequired(request.currency, "currency")
      val amountValidation = validateMinimumAbs(request.amount, 0.01, "amount")

      (currencyValidation, amountValidation).mapN(BankAccountUpdateRequest.apply)
    }
  }
}