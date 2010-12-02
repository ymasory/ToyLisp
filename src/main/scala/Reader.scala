package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String) = {
    import Parser._
    parseAll(toyForm, programText) match {
      case Success(ast, _) => Some(ast)
      case _ => None
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    lazy val program: Parser[List[ToyForm]] = (((ws*) ~> toyForm <~ (ws*))*)

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    lazy val lParen: Parser[String] = "("
    lazy val rParen: Parser[String] = ")"
    lazy val quote : Parser[String] = "'"
    lazy val ws    : Parser[String] = """\s+""".r

    lazy val toySymbol: Parser[ToySymbol] = """[a-zA-Z_@~%!=#\-\+\*\?\^\&]+""".r ^^ {ToySymbol(_)}
    lazy val toyChar  : Parser[ToyChar]   = quote ~> "[^.]".r <~ quote ^^ {s => ToyChar(s charAt 0)}
    lazy val toyNumber: Parser[ToyNumber] = floatingPointNumber ^^ {
      x => ToyNumber(x.toDouble)
    }

    //handle this with expansion handed to s-expression parser
    lazy val toyString: Parser[Any] = stringLiteral ^^ {_ => "STRING"} 

    lazy val toyForm: Parser[ToyForm] = toySymbol | toyNumber | toyChar
  }
}

sealed abstract class ToyForm
case class ToyChar(c: Char)       extends ToyForm
case class ToySymbol(str: String) extends ToyForm
case class ToyNumber(dub: Double) extends ToyForm
