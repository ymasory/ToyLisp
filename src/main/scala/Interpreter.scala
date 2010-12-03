package com.yuvimasory.toylisp

import scala.collection.{mutable => m}

/** A stateful interpreter.
  * Later invocations of `interpret` are affected by earlier ones.
  */
class Interpreter {

  def truthy = ToyNumber(1.0)
  def falsy = ToyNumber(0.0)
  def emptyList = ToyList(List())

  def isFalsy(form : ToyForm) : Boolean = {
    form match {
      case ToyCall(List()) => true
      case ToyNumber(0.0) => true
      case _ => false
    }
  }

  val environment = m.Map.empty[ToySymbol, ToyForm]

  def interpret(form: ToyForm): ToyForm = {
    form match {
      case ToyDo(stmts) => stmts.foldLeft (emptyList.asInstanceOf[ToyForm]) (
                                           (acc,form) => interpret(form))
      case ToyLambda(_, _) => form
      case ToyChar(_) | ToyNumber(_) | ToyList(_) => form
      case symb: ToySymbol => lookupSymbol(symb)
      case lst: ToyCall => functionApplication(lst)
    }
  }

  def lookupSymbol(symb: ToySymbol) = {
    val form = environment getOrElse (symb, throw UnboundSymbolError(symb.toString))
    interpret(form)
  }

  def functionApplication(lst: ToyCall): ToyForm = {
    lst match {
      case ToyCall(h :: t) =>
        h match {
         case ToyLambda(args, body) => {
           if (args.length == t.length) {
             for (i <- (0 until args.length)) {
               environment.update(args(i), interpret(t(i)))
             }
             interpret(body)
           } else {
             throw SyntaxError("tried to call a lambda with wrong number of args")
           }
         }
         case ToySymbol("set!") =>
                t match {
                  case List(ToySymbol(v), form) => {
                    environment.update(ToySymbol(v), interpret(form))
                    emptyList
                  }
                  case _ => throw SyntaxError("set needs a symbol and a form")
                }
         case ToySymbol("list?") =>
                (t map interpret) match {
                  case List(ToyList(_)) => truthy
                  case _ => falsy
                }
         case ToySymbol("char?") =>
                (t map interpret) match {
                  case List(ToyChar(_)) => truthy
                  case _ => falsy
                }
         case ToySymbol("num?") =>
                (t map interpret) match {
                  case List(ToyNumber(_)) => truthy
                  case _ => falsy
                }
         case ToySymbol("eq?") =>
                (t map interpret) match {
                  case List(ToyNumber(a), ToyNumber(b)) => if (a == b) truthy else falsy
                  case List(ToyChar(a), ToyChar(b)) => if (a == b) truthy else falsy
                  case List(ToyList(a), ToyList(b)) => if (a == b) truthy else falsy
                  case _ => falsy
                }
         case ToySymbol("char>num") =>
                (t map interpret) match {
                  case List(ToyChar(c)) => ToyNumber(c.toInt)
                  case _ => throw SyntaxError("char>num needs one char")
                }
          case ToySymbol("num>char") =>
                (t map interpret) match {
                  case List(ToyNumber(n)) => ToyChar(n.toChar)
                  case _ => throw SyntaxError("num>char needs one number")
                }
          case ToySymbol("+") =>
                (t map interpret) match {
                  case List(ToyNumber(a), ToyNumber(b)) => ToyNumber(a + b)
                  case _ => throw SyntaxError("plus needs two numbers")
                }
           case ToySymbol("opp") =>
                (t map interpret) match {
                  case List(ToyNumber(a)) => ToyNumber(-a)
                  case _ => throw SyntaxError("opp needs one number")
                }
           case ToySymbol("<=") =>
                (t map interpret) match {
                  case List(ToyNumber(a), ToyNumber(b)) =>
                    ToyNumber(if (a <= b) 1.0 else 0.0)
                  case _ => throw SyntaxError("plus needs two numbers")
                }
           case ToySymbol("floor") =>
                (t map interpret) match {
                  case List(ToyNumber(a)) =>
                    ToyNumber(java.lang.Math.floor(a))
                  case _ => throw SyntaxError("floor")
                }
           case ToySymbol("cons") =>
                (t map interpret) match {
                  case List(a, ToyList(q)) => ToyList(interpret(a) :: q)
                  case _ => throw SyntaxError("cons needs a toyform and a quoted list")
                }
           case ToySymbol("head") =>
                (t map interpret) match {
                  case List(ToyList(h :: t)) => h
                  case _ => throw SyntaxError("head needs a non-empty quoted list")
                }
           case ToySymbol("tail") =>
                (t map interpret) match {
                  case List(ToyList(h :: t)) => ToyList(t)
                  case _ => throw SyntaxError("tail needs a non-empty quoted list")
                }
           case ToySymbol("if") =>
                t match {
                  case List(cond, ift, iff) => {
                    interpret(  if (isFalsy(interpret(cond))) ift else iff  )
                  }
                  case _ => throw SyntaxError("if")
                }
           case _ => lst
        }
      case _ => lst
    }
  }
}

case class UnboundSymbolError(msg: String) extends Exception(msg)
case class SyntaxError(msg: String) extends Exception(msg)

