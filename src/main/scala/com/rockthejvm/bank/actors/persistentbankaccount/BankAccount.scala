package com.rockthejvm.bank.actors.persistentbankaccount

// state
case class BankAccount(id: String, user: String, currency: String, balance: Double)