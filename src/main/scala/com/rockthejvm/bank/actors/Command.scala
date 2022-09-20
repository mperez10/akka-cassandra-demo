package com.rockthejvm.bank.actors

import akka.actor.typed.ActorRef

// commands = messages
sealed trait Command

case class CreateBankAccount(user: String, currency: String, initialBalance: Double, replyTo: ActorRef[Response]) extends Command
case class UpdateBalance(id: String, currency: String, amount: Double /* can be < 0*/ , replyTo: ActorRef[Response]) extends Command
case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command