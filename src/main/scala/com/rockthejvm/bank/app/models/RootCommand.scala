package com.rockthejvm.bank.app.models

import akka.actor.typed.ActorRef
import com.rockthejvm.bank.actors.Command

trait RootCommand
case class RetrieveBankActor(replyTo: ActorRef[ActorRef[Command]]) extends RootCommand
