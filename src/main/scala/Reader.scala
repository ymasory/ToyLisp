package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String): Option[ToyUnquotedList] = {
    import Parser._
    parseAll(program, programText) match {
      case Success(ast, _) => Some(ast)
      case _ => None
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    lazy val program: Parser[ToyUnquotedList] = (ws*) ~> toyUnquotedList <~ (ws*) ^^ {
      case ToyUnquotedList(exprs) => ToyUnquotedList(exprs)
    }

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

    private lazy val toyUnquotedList: Parser[ToyUnquotedList] = lParen ~> (ws*) ~> (toyExpression*) <~ 
      (ws*) <~ rParen ^^ {ToyUnquotedList(_)}
    private lazy val toyQuotedList: Parser[ToyQuotedList] = quote ~> toyUnquotedList ^^ {
        case ToyUnquotedList(lst) => ToyQuotedList(lst)
    }
  }
}

sealed abstract class ToyExpression
case class ToyString(str: String) extends ToyExpression
case class ToyToken(str: String) extends ToyExpression
case class ToyNumber(dub: Double) extends ToyExpression

sealed abstract class ToyList
case class ToyQuotedList(lst: List[ToyExpression]) extends ToyList
case class ToyUnquotedList(lst: List[ToyExpression]) extends ToyList
