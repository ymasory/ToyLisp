package com.yuvimasory.toylisp

import scala.collection.{mutable => m}

/** A stateful interpreter.
  * Later invocations of `interpret` are affected by earlier ones.
  */
class Interpreter {

  val environment = m.Map.empty[ToySymbol, ToyForm]

  def interpret(form: ToyForm): ToyForm = {
    form match {
      case ToyChar(_) | ToyNumber(_) | ToyQList(_) => form
      case symb: ToySymbol => lookupSymbol(symb)
      case lst: ToyList => functionApplication(lst)
    }
  }

  def lookupSymbol(symb: ToySymbol) = {
    val form = environment getOrElse (symb, throw UnboundSymbolError(symb.toString))
    interpret(form)
  }

  def functionApplication(lst: ToyList): ToyForm = {
    lst match {
      case ToyList(h :: t) => lst
      case _ => lst
    }
  }
}

case class UnboundSymbolError(msg: String) extends Exception(msg)
case class SyntaxError(msg: String) extends Exception(msg)

