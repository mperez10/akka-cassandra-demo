package com.rockthejvm.bank.http.bankRoute

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import cats.data.Validated.{Invalid, Valid}
import com.rockthejvm.bank.actors.PersistentBankAccount.Command._
import com.rockthejvm.bank.actors.PersistentBankAccount.{Command, Response}
import com.rockthejvm.bank.actors.PersistentBankAccount.Response._
import com.rockthejvm.bank.http.validations.Validation.{Validator, validateEntity}
import com.rockthejvm.bank.http.models.BankAccountCreationRequest.validator
import com.rockthejvm.bank.http.models.{BankAccountCreationRequest, BankAccountUpdateRequest, FailureResponse}
import com.rockthejvm.bank.http.validations.ValidatableRequest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class BankRouter(bank: ActorRef[Command])(implicit system: ActorSystem[_]) extends BankRoutes with ValidatableRequest{
  implicit val timeout: Timeout = Timeout(5.seconds)

  private def createBankAccount(request: BankAccountCreationRequest): Future[Response] =
    bank.ask(replyTo => request.toCommand(replyTo))

  private def postBankAccountCreation: Route = {
    // parse the payload
    entity(as[BankAccountCreationRequest]) { request =>
      // validation
      validateRequest(request) {
        /*
                - convert the request into a Command for the bank actor
                - send the command to the bank
                - expect a reply
               */
        onSuccess(createBankAccount(request)) {
          // send back an HTTP response
          case BankAccountCreatedResponse(id) =>
            respondWithHeader(Location(s"/bank/$id")) {
              complete(StatusCodes.Created)
            }
        }
      }
    }
  }

  private def getBankAccount(id: String): Future[Response] =
    bank.ask(replyTo => GetBankAccount(id, replyTo))

  private def getBankAccountInformation(id: String): Route = {
    /*
              - send command to the bank
              - expect a reply
              */
    onSuccess(getBankAccount(id)) {
      //  - send back the HTTP response
      case GetBankAccountResponse(Some(account)) =>
        complete(account) // 200 OK
      case GetBankAccountResponse(None) =>
        complete(StatusCodes.NotFound, FailureResponse(s"Bank account $id cannot be found."))
    }
  }

  private def updateBankAccount(id: String, request: BankAccountUpdateRequest): Future[Response] =
    bank.ask(replyTo => request.toCommand(id, replyTo))

  private def putBankAccountUpdate(id: String): Route = {
    entity(as[BankAccountUpdateRequest]) { request =>
      // validation
      validateRequest(request) {
        /*
                      - transform the request to a Command
                      - send the command to the bank
                      - expect a reply
                     */
        onSuccess(updateBankAccount(id, request)) {
          // send HTTP response
          case BankAccountBalanceUpdatedResponse(Success(account)) =>
            complete(account)
          case BankAccountBalanceUpdatedResponse(Failure(ex)) =>
            complete(StatusCodes.BadRequest, FailureResponse(s"${ex.getMessage}"))
        }
      }
    }
  }

  def routesBank: Route = routesBank(postBankAccountCreation, getBankAccountInformation, putBankAccountUpdate)
}
