package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String): Option[List[ToyList]] = {
    import Parser._
    parseAll(program, programText) match {
      case Success(ast, _) => Some(ast)
      case _ => None
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    lazy val program: Parser[List[ToyList]] = (ws*) ~> (toyList*) <~ (ws*)

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    private lazy val lParen: Parser[String] = "("
    private lazy val rParen: Parser[String] = ")"
    private lazy val quote : Parser[String] = "'"
    private lazy val ws    : Parser[String] = """\s+""".r

    private lazy val toyToken : Parser[ToyToken]  = "[a-zA-Z_+-*/%!=&^]+".r ^^ {ToyToken(_)}
    private lazy val toyString: Parser[ToyString] = "[^\"]*?".r             ^^ {ToyString(_)}
    private lazy val toyNumber: Parser[ToyNumber] = floatingPointNumber ^^ {
      x => ToyNumber(x.toDouble)
    }

    private lazy val toyExpression: Parser[ToyExpression] = toyToken | toyNumber | toyString

    private lazy val toyFunctionList: Parser[ToyFunctionList] = lParen ~> (ws*) ~> (toyExpression*) <~
      (ws*) <~ rParen ^^ {ToyFunctionList(_)}
    private lazy val toyExpressionList: Parser[ToyExpressionList] = quote ~> toyFunctionList ^^ {
        case ToyFunctionList(lst) => ToyExpressionList(lst)
    }
    private lazy val toyList: Parser[ToyList] = toyFunctionList | toyExpressionList
  }
}

sealed abstract class ToyExpression
case class ToyString(str: String) extends ToyExpression
case class ToyToken(str: String) extends ToyExpression
case class ToyNumber(dub: Double) extends ToyExpression

sealed abstract class ToyList(lst: List[ToyExpression])
case class ToyExpressionList(lst: List[ToyExpression]) extends ToyList(lst)
case class ToyFunctionList(lst: List[ToyExpression]) extends ToyList(lst)

