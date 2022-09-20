package com.rockthejvm.bank.actors.bank

// events
sealed trait Event
case class BankAccountCreated(id: String) extends Event
