package com.rockthejvm.bank.http

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import com.rockthejvm.bank.actors.Command
import com.rockthejvm.bank.http.bankRoute.BankRouter

class MainRouter(bankActor: ActorRef[Command])(implicit system: ActorSystem[_]) {
  val routerBank = new BankRouter(bankActor)
  val routes: Route = routerBank.routesBank
}
