package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String): Option[AST] = {
    import Parser._
    parseAll(program, programText) match {
      case Success(ast, _) => Some(ast)
      case _ => None
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    lazy val lParen: Parser[String] = "("
    lazy val rParen: Parser[String] = ")"
    lazy val quote : Parser[String] = "'"
    lazy val ws    : Parser[String] = """\s*""".r

    lazy val toyToken : Parser[ToyToken]  = "[a-zA-Z_+-*/%!=&^]+".r ^^ {ToyToken(_)}
    lazy val toyString: Parser[ToyString] = "[^\"]*?".r             ^^ {ToyString(_)}
    lazy val toyNumber: Parser[ToyNumber] = floatingPointNumber     ^^ {x => ToyNumber(x.toDouble)}

    lazy val toyExpression: Parser[ToyExpression] = toyToken | toyNumber | toyString

    lazy val program: Parser[AST] = null
  }
}


case class AST(strs: List[String])
sealed abstract class ToyExpression
case class ToyString(str: String) extends ToyExpression
case class ToyToken(str: String) extends ToyExpression
case class ToyNumber(dub: Double) extends ToyExpression
