package com.yuvimasory.toylisp

import scala.util.parsing.combinator._

object Reader {


  def read(programText: String): Either[String, ToyForm] = {
    import Parser._
    parseAll(toyProgram, programText) match {
      case Success(form, _) => Right(recognizeSpecialForms(form))
      case NoSuccess(msg, _) => Left(msg)
    }
  }

  private[toylisp] object Parser extends RegexParsers with JavaTokenParsers {

    //in the tradition of Lisp, a program is a list of forms
    lazy val toyProgram: Parser[ToyForm] = (((ws*) ~> toyForm <~ (ws*))*) ^^ {ToyList(_)}

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
    lazy val toyString: Parser[ToyCall] = stringLiteral ^^ {
      str => {
        val chars = str.substring(1, str.length - 1).toList.map(quoteStr + _ + quoteStr)
        val sExpr = "(" + chars.mkString(" ") + ")"
        parse(toyCall, sExpr).get
      }
    }

    //list types parser
    lazy val toyCall: Parser[ToyCall] = lParen ~> (((ws*) ~> toyForm <~ (ws*))*) <~ rParen ^^ {
      forms => ToyCall(forms)
    }
    lazy val toyList: Parser[ToyList] = quote ~> toyCall ^^ {
      case ToyCall(lists) => ToyList(lists)
    }

    //"primitive types", list types, and sugar types together make all the forms
    lazy val toyForm: Parser[ToyForm] = toySymbol | toyNumber | toyChar | toyCall | toyList | toyString
  }

  def isSymbol(form : ToyForm) : Boolean = {
    form match {
      case ToySymbol(_) => true
      case _ => false
    }
  }

  def recognizeSpecialForms(form: ToyForm): ToyForm = {
    form match {
      case ToyLambda(_, _) | ToyDo(_) => form // not actually going to get called...
      case ToyList(q) => ToyList(q map recognizeSpecialForms)
      case ToyCall(List(ToySymbol("lambda"),
                        ToyCall(args),
                        body)) =>
         if (args.forall (a => isSymbol(a))) {
           ToyLambda(  (for (ToySymbol(a) <- args) yield ToySymbol(a)).toList,
                     recognizeSpecialForms(body))
         } else {
           throw SyntaxError("lambda requires only symbol names in arg list")
         }
      case ToyCall( ToySymbol("do") :: stmts ) => ToyDo(stmts)
      case ToyChar(_) | ToyNumber(_) | ToySymbol(_) => form
      case ToyCall(forms) => ToyCall(forms map recognizeSpecialForms)
    }
  }
}

sealed abstract class ToyForm
case class ToyDo(stmts : List[ToyForm]) extends ToyForm {
  override val toString = "(do " + stmts.mkString(" ") + ")"
}
case class ToyLambda(args : List[ToySymbol], body: ToyForm) extends ToyForm {
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
case class ToyCall  (lst: List[ToyForm]) extends ToyForm {
  override val toString = "(" + lst.mkString(" ") + ")"
}
case class ToyList (lst: List[ToyForm]) extends ToyForm {
  override val toString = "[" + lst.mkString(" ") + "]"
}
