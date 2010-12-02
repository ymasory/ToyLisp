package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String) = {
    import Parser._
    parseAll(program, programText) match {
      case Success(ast, _) => Some(ast)
      case _ => None
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    lazy val program: Parser[List[ToyList]] = ((ws*) ~> toyList <~ (ws*))*

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    lazy val lParen: Parser[String] = "("
    lazy val rParen: Parser[String] = ")"
    lazy val quote : Parser[String] = "'"
    lazy val dQuote: Parser[String] = "\""
    lazy val ws    : Parser[String] = """\s+""".r

    lazy val toyToken : Parser[ToyToken]  = """[a-zA-Z_@~%!=#\-\+\*\?\^\&]+""".r ^^ {ToyToken(_)}
    lazy val toyString: Parser[ToyString] = dQuote ~> "[^\"]*".r <~ dQuote ^^ {ToyString(_)}
    lazy val toyNumber: Parser[ToyNumber] = floatingPointNumber ^^ {
      x => ToyNumber(x.toDouble)
    }

    lazy val toyExpression: Parser[ToyExpression] = toyToken | toyNumber | toyString

    lazy val toyFunctionList: Parser[ToyFunctionList] = lParen ~> (ws*) ~> toyToken ~ (((ws+) ~> toyExpression <~ 
      (ws*))*) <~ rParen ^^ {
        case token ~ exprs => ToyFunctionList(token :: exprs)
      }
    lazy val toyExpressionList: Parser[ToyExpressionList] = quote ~> toyFunctionList ^^ {
        case ToyFunctionList(lst) => ToyExpressionList(lst)
    }
    lazy val toyList: Parser[ToyList] = toyFunctionList | toyExpressionList
  }
}

sealed abstract class ToyExpression
case class ToyString(str: String) extends ToyExpression
case class ToyToken(str: String) extends ToyExpression
case class ToyNumber(dub: Double) extends ToyExpression

sealed abstract class ToyList(lst: List[ToyExpression])
case class ToyExpressionList(lst: List[ToyExpression]) extends ToyList(lst)
case class ToyFunctionList(lst: List[ToyExpression]) extends ToyList(lst)

