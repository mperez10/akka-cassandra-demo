package com.rockthejvm.bank.actors.bank

import akka.actor.typed.ActorRef
import com.rockthejvm.bank.actors.Command

// state
case class State(accounts: Map[String, ActorRef[Command]])