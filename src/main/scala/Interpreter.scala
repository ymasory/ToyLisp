package com.yuvimasory.toylisp

import scala.collection.{mutable => m}

/** A stateful interpreter.
  * Later invocations of `interpret` are affected by earlier ones.
  */
class Interpreter {

  def interpret(form: ToyForm): ToyForm = {
    form match {
      case ToyDo(stmts) => stmts.foldLeft(emptyList.asInstanceOf[ToyForm]){
        (_, form) => interpret(form)
      }
      case ToyLambda(_, _) => form
      case ToyChar(_) | ToyNumber(_) | ToyList(_) => form
      case symb: ToySymbol => lookupSymbol(symb)
      case lst: ToyCall => functionApplication(lst)
    }
  }

  private val one = ToyNumber(1.0)
  private val zero = ToyNumber(0.0)
  private val emptyList = ToyList(Nil)

  private def falsy(form: ToyForm): Boolean = {
    form match {
      case ToyCall(Nil) => true
      case `zero` => true
      case _ => false
    }
  }

  private val environment = m.Map.empty[ToySymbol, ToyForm]

  private def lookupSymbol(symb: ToySymbol) = {
    val form = environment getOrElse (symb, throw UnboundSymbolError(symb.toString))
    interpret(form)
  }

  private def handleLambda(lambda: ToyLambda, forms: List[ToyForm]) = {
    lambda match {
      case ToyLambda(args, body) => {
        if (args.length == forms.length) {
          for (i <- (0 until args.length)) {
            environment.update(args(i), interpret(forms(i)))
          }
          interpret(body)
        }
        else throw SyntaxError("tried to call a lambda with wrong number of args")
      }
    }
  }

  private def functionApplication(toyCall: ToyCall): ToyForm = {
    toyCall match {
      case ToyCall(firstForm :: restForms) => firstForm match {
        case tl: ToyLambda => handleLambda(tl, restForms)
        case ToySymbol("set!") => restForms match {
          case List(ToySymbol(v), form) => {
            environment.update(ToySymbol(v), interpret(form))
            emptyList
          }
          case _ => throw SyntaxError("set needs a symbol and a form")
        }
        case ToySymbol("list?") => (restForms map interpret) match {
          case List(ToyList(_)) => one
          case _ => zero
        }
        case ToySymbol("char?") => (restForms map interpret) match {
          case List(ToyChar(_)) => one
          case _ => zero
        }
        case ToySymbol("num?") => (restForms map interpret) match {
          case List(ToyNumber(_)) => one
          case _ => zero
        }
        case ToySymbol("eq?") => (restForms map interpret) match {
          case List(ToyNumber(a), ToyNumber(b)) => if (a == b) one else zero
          case List(ToyChar(a), ToyChar(b)) => if (a == b) one else zero
          case List(ToyList(a), ToyList(b)) => if (a == b) one else zero
          case _ => zero
        }
        case ToySymbol("char>num") => (restForms map interpret) match {
          case List(ToyChar(c)) => ToyNumber(c.toInt)
          case _ => throw SyntaxError("char>num needs one char")
        }
        case ToySymbol("num>char") => (restForms map interpret) match {
          case List(ToyNumber(n)) => ToyChar(n.toChar)
          case _ => throw SyntaxError("num>char needs one number")
        }
        case ToySymbol("+") => (restForms map interpret) match {
          case List(ToyNumber(a), ToyNumber(b)) => ToyNumber(a + b)
          case _ => throw SyntaxError("plus needs two numbers")
        }
        case ToySymbol("opp") => (restForms map interpret) match {
          case List(ToyNumber(a)) => ToyNumber(-a)
          case _ => throw SyntaxError("opp needs one number")
        }
        case ToySymbol("<=") => (restForms map interpret) match {
          case List(ToyNumber(a), ToyNumber(b)) => if (a <= b) one else zero
          case _ => throw SyntaxError("plus needs two numbers")
        }
        case ToySymbol("floor") => (restForms map interpret) match {
          case List(ToyNumber(a)) => ToyNumber(java.lang.Math.floor(a))
          case _ => throw SyntaxError("floor requires one argument")
        }
        case ToySymbol("cons") => (restForms map interpret) match {
          case List(a, ToyList(q)) => ToyList(interpret(a) :: q)
          case _ => throw SyntaxError("cons needs a form and a list")
        }
        case ToySymbol("head") => (restForms map interpret) match {
          case List(ToyList(h :: t)) => h
          case _ => throw SyntaxError("head needs a non-empty quoted list")
        }
        case ToySymbol("tail") => (restForms map interpret) match {
          case List(ToyList(h :: t)) => ToyList(t)
          case _ => throw SyntaxError("tail needs a non-empty quoted list")
        }
        case ToySymbol("if") => restForms match {
          case List(cond, ift, iff) => interpret(  if (falsy(interpret(cond))) ift else iff  )
          case _ => throw SyntaxError("if requires three arguments")
        }
        case userFunc => {
          interpret(userFunc) match {
            case tl: ToyLambda => toyCall //not sure what to do here so just returning the input
            case _ => throw SyntaxError("first element of a function call must be" + 
                                        " the lambda keyword or result in a lambda")
          }
        }
      }
      case _ => throw SyntaxError("use [] for empty list")
    }
  }
}

case class UnboundSymbolError(msg: String) extends Exception(msg)
case class SyntaxError(msg: String) extends Exception(msg)

