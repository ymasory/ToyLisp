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

    //in the tradition of Lisp, a program is a list of lists
    lazy val program: Parser[ToyList] = (((ws*) ~> toyList <~ (ws*))*) ^^ {ToyList(_)}

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    lazy val quoteStr: String = "'"

    //handy string/regex parsers
    lazy val lParen: Parser[String] = "("
    lazy val rParen: Parser[String] = ")"
    lazy val quote : Parser[String] = quoteStr
    lazy val ws    : Parser[String] = """\s+""".r

    //"primitive types" parsers
    lazy val toySymbol: Parser[ToySymbol] = """[a-zA-Z_@~%!=#\-\+\*\?\^\&]+""".r ^^ {ToySymbol(_)}
    lazy val toyChar  : Parser[ToyChar]   = quote ~> "[^.]".r <~ quote ^^ {s => ToyChar(s charAt 0)}
    lazy val toyNumber: Parser[ToyNumber] = floatingPointNumber ^^ {
      str => ToyNumber(str.toDouble)
    }

    //syntactic sugar parsers
    lazy val toyString: Parser[ToyList] = stringLiteral ^^ {
      str => {
        val chars = str.toList.map(quoteStr + _ + quoteStr)
        val sExpr = "(" + chars.mkString(" ") + ")"
        parse(toyList, sExpr).get
      }
    } 

    //list types parser
    lazy val toyList: Parser[ToyList] = lParen ~> (((ws*) ~> toyForm <~ (ws*))*) <~ rParen ^^ {
      forms => ToyList(forms)
    }
    lazy val toyQList: Parser[ToyQList] = quote ~> toyList ^^ {
      case ToyList(lists) => ToyQList(lists)
    }

    //"primitive types", list types, and sugar types together make all the forms
    lazy val toyForm: Parser[ToyForm] = toySymbol | toyNumber | toyChar | toyList | toyQList | toyString
  }
}

sealed abstract class ToyForm
case class ToyChar  (chr: Char)          extends ToyForm {
  override val toString = chr.toString
}
case class ToyNumber(dub: Double)        extends ToyForm {
  override val toString = dub.toString
}
case class ToySymbol(str: String)        extends ToyForm {
  override val toString = str
}
case class ToyList  (lst: List[ToyForm]) extends ToyForm {
  override val toString = "(" + lst.mkString(" ") + ")"
}
case class ToyQList (lst: List[ToyForm]) extends ToyForm {
  override val toString = "(" + lst.mkString(" ") + ")"
}
