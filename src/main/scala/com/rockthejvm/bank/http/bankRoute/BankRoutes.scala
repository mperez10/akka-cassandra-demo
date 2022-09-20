package com.rockthejvm.bank.http.bankRoute

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait BankRoutes {
  /*
    POST /bank/
      Payload: bank account creation request as JSON
      Response:
        201 Created
        Location: /bank/uuid

    GET /bank/uuid
      Response:
        200 OK
        JSON repr of bank account details

        404 Not found

    PUT /bank/uuid
      Payload: (currency, amount) as JSON
      Response:
        1)  200 OK
            Payload: new bank details as JSON
        2)  404 Not found
   */
  type StringToRoute = String => Route
  val rootEndpoint = "bank907"

  def routesBank(postBankAccountCreation: Route,
                 getBankAccountInformation: StringToRoute,
                 putBankAccountUpdate: StringToRoute): Route =
    pathPrefix(rootEndpoint) {
      pathEndOrSingleSlash {
        post {
          postBankAccountCreation
        }
      } ~
        path(Segment) { id =>
          get {
            getBankAccountInformation(id)
          } ~
            put {
              putBankAccountUpdate(id)
            }
        }
    }
}
