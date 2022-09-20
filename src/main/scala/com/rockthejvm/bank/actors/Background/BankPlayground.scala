package com.rockthejvm.bank.actors.Background

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import com.rockthejvm.bank.actors.{BankAccountCreatedResponse, GetBankAccountResponse, Response}
import com.rockthejvm.bank.actors.bank.Bank
import com.rockthejvm.bank.actors.persistentbankaccount._

import scala.concurrent.ExecutionContext


object BankPlayground {

  def main(args: Array[String]): Unit = {
    val rootBehavior: Behavior[NotUsed] = Behaviors.setup { context =>
      val bank = context.spawn(Bank(), "bank")
      val logger = context.log

      val responseHandler = context.spawn(Behaviors.receiveMessage[Response]{
        case BankAccountCreatedResponse(id) =>
          logger.info(s"successfully created bank account $id")
          Behaviors.same
        case GetBankAccountResponse(maybeBankAccount) =>
          logger.info(s"Account details: $maybeBankAccount")
          Behaviors.same
      }, "replyHandler")

      // ask pattern
      import akka.actor.typed.scaladsl.AskPattern._
      import scala.concurrent.duration._
      implicit val timeout: Timeout = Timeout(2.seconds)
      implicit val scheduler: Scheduler = context.system.scheduler
      implicit val ec: ExecutionContext = context.executionContext

      //      bank ! CreateBankAccount("daniel", "USD", 10, responseHandler)
      //      bank ! GetBankAccount("deda8465-ddc3-4988-a584-4019d55a3045", responseHandler)

      Behaviors.empty
    }

    val system = ActorSystem(rootBehavior, "BankDemo")
  }
}