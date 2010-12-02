package com.yuvimasory.toylisp

import scala.collection.{mutable => m}

/** A stateful interpreter.
  * Later invocations of `interpret` are affected by earlier ones.
  */
class Interpreter {

  val environment = m.Map.empty[ToySymbol, ToyForm]

  def interpret(form: ToyForm): ToyForm = {
    form match {
      case ToyChar(_) | ToyNumber(_) => form
      case symb: ToySymbol => lookupSymbol(symb)
      case lst: ToyList => functionApplication(lst)
      case lst: ToyQList => makeList(lst)
    }
  }

  def lookupSymbol(symb: ToySymbol) = {
    environment getOrElse (symb, throw UnboundSymbolError(symb.toString))
  }

  def functionApplication(lst: ToyList): ToyForm = lst

  def makeList(lst: ToyQList): ToyForm = lst
}

case class UnboundSymbolError(msg: String) extends Exception(msg)
case class SyntaxError(msg: String) extends Exception(msg)
