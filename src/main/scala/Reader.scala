package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String): Either[String, ToyForm] = {
    import Parser._
    parseAll(toyProgram, programText) match {
      case Success(form, _) => Right(recognizeLambdas(form))
      case NoSuccess(msg, _) => Left(msg)
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    //in the tradition of Lisp, a program is a list of forms
    lazy val toyProgram: Parser[ToyForm] = (((ws*) ~> toyForm <~ (ws*))*) ^^ {ToyQList(_)}

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
        val chars = str.substring(1, str.length - 1).toList.map(quoteStr + _ + quoteStr)
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

  def recognizeLambdas(form: ToyForm): ToyForm = {
    form match {
      case ToyLambda(_, _) => form // not actually going to get called...
      case ToyQList(q) => ToyQList(q map recognizeLambdas)
      case ToyList(List(ToySymbol("lambda"),
                        ToyList(List(ToySymbol(argname))),
                        body)) =>
           ToyLambda(List(argname), recognizeLambdas(body))

      case ToyChar(_) | ToyNumber(_) | ToySymbol(_) => form
      case ToyList(forms) => ToyList(forms map recognizeLambdas)
    }
  }
}

sealed abstract class ToyForm
case class ToyLambda(args : List[String], body: ToyForm) extends ToyForm {
  override val toString = "<lambda " + args.toString + " -> " + body.toString + ">"
}
case class ToyChar  (chr: Char)          extends ToyForm {
  override val toString = "'" + chr.toString + "'"
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
