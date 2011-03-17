package com.yuvimasory.toylisp

import java.io.{ BufferedReader, File, InputStreamReader, OutputStreamWriter }

import scala.collection.{ mutable => m }
import scala.io.Source
import scala.util.parsing.combinator.{ RegexParsers, JavaTokenParsers }

import jline.{ ConsoleReader, History }

/* BEGIN PARSER */
object Reader extends RegexParsers with JavaTokenParsers {

  def read(programText: String): Either[String, ToyList] = {
    parseAll(toyProgram, programText) match {
      case Success(form, _) => Right(form)
      case NoSuccess(msg, _) => Left(msg)
    }
  }

  //in the tradition of Lisp, a program is a list of forms
  lazy val toyProgram: Parser[ToyList] =
    (((whiteSpace*) ~> toyForm <~ (whiteSpace*))*) ^^ { ToyList(_) }

  lazy val QuoteStr: String = "'"

  //handy string/regex parsers
  lazy val lParen: Parser[String] = "("
  lazy val rParen: Parser[String] = ")"
  lazy val lBrack: Parser[String] = "["
  lazy val rBrack: Parser[String] = "]"
  lazy val quote: Parser[String] = QuoteStr

  //parsers for "primitive types"
  lazy val toySymbol: Parser[ToySymbol] =
    """[a-zA-Z_@~%!=#<>\+\*\?\^\&]+""".r ^^ { ToySymbol(_) }
  lazy val toyChar: Parser[ToyChar] =
    quote ~> ".".r <~ quote ^^ { s => ToyChar(s.head) }
  lazy val toyInt: Parser[ToyInt] = wholeNumber ^^ { str =>
    ToyInt(str.toInt)
  }

  /**
   * ToyLisp has no string "type", just string ``syntax`` for lists of
   * characters.
   * So we will rewrite string literals into lists of characters and parse that.
   */
  lazy val toyString: Parser[ToyCall] = stringLiteral ^^ { str =>
    {
      val chars =
        str.substring(1, str.length - 1).toList.map(QuoteStr + _ + QuoteStr)
      val sExpr = "(" + chars.mkString(" ") + ")"
      parse(toyCall, sExpr).get
    }
  }

  //parser for function application
  lazy val toyCall: Parser[ToyCall] =
    lParen ~> (toyForm*) <~ rParen ^^ { ToyCall(_) }

  lazy val toyList: Parser[ToyList] =
    lBrack ~> (toyForm+) <~ rBrack ^^ { ToyList(_) }

  //parsers for lambdas
  lazy val toyListOfSymbols: Parser[List[ToySymbol]] = lBrack ~> (toySymbol+) <~ rBrack
  lazy val toyLambda: Parser[ToyLambda] =
    lParen ~> "lambda".r ~> toyListOfSymbols ~ toyCall <~ rParen ^^ {
      case lst ~ call => ToyLambda(lst, call)
    }

  //"primitive types", list types, and sugar types together make all the forms
  lazy val toyForm: Parser[ToyForm] =
    toySymbol | toyInt | toyChar | toyCall | toyList | toyString | toyLambda
}

// Algebraic data types for target language terms
sealed abstract class ToyForm
case class ToyChar(chr: Char) extends ToyForm
case class ToyInt(i: Int) extends ToyForm
case class ToySymbol(str: String) extends ToyForm
case class ToyList(lst: List[ToyForm]) extends ToyForm
sealed abstract class AbstractToyCall extends ToyForm
case class ToyCall(lst: List[ToyForm]) extends AbstractToyCall
case class ToyLambda(args: List[ToySymbol], body: ToyCall) extends AbstractToyCall


/* END PARSER */

/* BEGIN INTERPRETER */
object Interpreter {

  def eval(form: ToyForm): ToyForm = {
    null
    // form match {
    //   case ToyDo(stmts) => stmts.foldLeft(
    //     emptyList.asInstanceOf[ToyForm]) { (_, form) =>
    //     eval(form)
    //   }
    //   case ToyLambda(_, _) => form
    //   case ToyChar(_) | ToyInt(_) | ToyList(_) => form
    //   case symb: ToySymbol => lookupSymbol(symb)
    //   case lst: ToyCall => functionApplication(lst)
    // }
  }

  // private val one = ToyInt(1)
  // private val zero = ToyInt(0)
  // private val emptyList = ToyList(Nil)

  // private def falsy(form: ToyForm): Boolean = {
  //   form match {
  //     case ToyCall(Nil) => true
  //     case `zero` => true
  //     case _ => false
  //   }
  // }

  // private val environment = m.Map.empty[ToySymbol, ToyForm]

  // private def lookupSymbol(symb: ToySymbol) = {
  //   val form = environment getOrElse (symb,
  //     throw UnboundSymbolError(symb.toString))
  //   eval(form)
  // }

  // private def handleLambda(lambda: ToyLambda, forms: List[ToyForm]) = {
  //   lambda match {
  //     case ToyLambda(args, body) => {
  //       if (args.length == forms.length) {
  //         for (i <- (0 until args.length)) {
  //           environment.update(args(i), eval(forms(i)))
  //         }
  //         eval(body)
  //       } else
  //         throw SyntaxError("tried to call a lambda with wrong number of args")
  //     }
  //   }
  // }

  // private def functionApplication(toyCall: ToyCall): ToyForm = {
  //   toyCall match {
  //     case ToyCall(firstForm :: restForms) => firstForm match {
  //       case tl: ToyLambda => handleLambda(tl, restForms)
  //       case ToySymbol("set!") => restForms match {
  //         case List(ToySymbol(v), form) => {
  //           environment.update(ToySymbol(v), eval(form))
  //           emptyList
  //         }
  //         case _ => throw SyntaxError("set needs a symbol and a form")
  //       }
  //       case ToySymbol("list?") => (restForms map eval) match {
  //         case List(ToyList(_)) => one
  //         case _ => zero
  //       }
  //       case ToySymbol("char?") => (restForms map eval) match {
  //         case List(ToyChar(_)) => one
  //         case _ => zero
  //       }
  //       case ToySymbol("num?") => (restForms map eval) match {
  //         case List(ToyInt(_)) => one
  //         case _ => zero
  //       }
  //       case ToySymbol("eq?") => (restForms map eval) match {
  //         case List(ToyInt(a), ToyInt(b)) => if (a == b) one else zero
  //         case List(ToyChar(a), ToyChar(b)) => if (a == b) one else zero
  //         case List(ToyList(a), ToyList(b)) => if (a == b) one else zero
  //         case _ => zero
  //       }
  //       case ToySymbol("char>num") => (restForms map eval) match {
  //         case List(ToyChar(c)) => ToyInt(c.toInt)
  //         case _ => throw SyntaxError("char>num needs one char")
  //       }
  //       case ToySymbol("num>char") => (restForms map eval) match {
  //         case List(ToyInt(n)) => ToyChar(n.toChar)
  //         case _ => throw SyntaxError("num>char needs one number")
  //       }
  //       case ToySymbol("+") => (restForms map eval) match {
  //         case List(ToyInt(a), ToyInt(b)) => ToyInt(a + b)
  //         case _ => throw SyntaxError("plus needs two numbers")
  //       }
  //       case ToySymbol("opp") => (restForms map eval) match {
  //         case List(ToyInt(a)) => ToyInt(-a)
  //         case _ => throw SyntaxError("opp needs one number")
  //       }
  //       case ToySymbol("<=") => (restForms map eval) match {
  //         case List(ToyInt(a), ToyInt(b)) => if (a <= b) one else zero
  //         case _ => throw SyntaxError("plus needs two numbers")
  //       }
  //       case ToySymbol("cons") => (restForms map eval) match {
  //         case List(a, ToyList(q)) => ToyList(eval(a) :: q)
  //         case _ => throw SyntaxError("cons needs a form and a list")
  //       }
  //       case ToySymbol("head") => (restForms map eval) match {
  //         case List(ToyList(h :: t)) => h
  //         case _ => throw SyntaxError("head needs a non-empty quoted list")
  //       }
  //       case ToySymbol("tail") => (restForms map eval) match {
  //         case List(ToyList(h :: t)) => ToyList(t)
  //         case _ => throw SyntaxError("tail needs a non-empty quoted list")
  //       }
  //       case ToySymbol("if") => restForms match {
  //         case List(cond, ift, iff) => eval(if (falsy(eval(cond))) ift
  //         else iff)
  //         case _ => throw SyntaxError("if requires three arguments")
  //       }
  //       case userFunc => {
  //         eval(userFunc) match {
  //           case tl: ToyLambda => handleLambda(tl, restForms)
  //           case _ => throw SyntaxError("first element of a function call" +
  //             " must be the lambda keyword or result in a lambda")
  //         }
  //       }
  //     }
  //     case _ => throw SyntaxError("use [] for empty list")
  //   }
  // }
}

case class UnboundSymbolError(msg: String) extends Exception(msg)
case class SyntaxError(msg: String) extends Exception(msg)
/* END INTERPRETER */

/* BEGIN MAIN */
object Main {

  /**
   * Use JLine's ConsoleReader instead of Console.in, just so we can
   * hit backspace in interactive mode, have command history, etc.
   */
  val in = {
    val consoleReader = new ConsoleReader(System.in,
      new OutputStreamWriter(System.out))
    consoleReader setHistory (new History(new File(".toyhistory")))
    consoleReader setUseHistory true
    consoleReader setDefaultPrompt ">> "
    consoleReader
  }

  /**
   * Run the standard library, then either run the provided file,
   * or go into interactive mode if no file is provided.
   */
  def main(args: Array[String]) {
    runInteractive()
  }

  def runInteractive() {
    println("\nWelcome to Toy Lisp! Press Ctrl+D to exit.\n")
    while (true) {
      in.readLine() match {
        case input: String => giveOutput(input)
        case _ => return println("okbye!")
      }
    }
  }

  /**
   * Interpret the programText, then if `quiet` is `false` prettily display
   * the output.
   */
  private def giveOutput(programText: String, quiet: Boolean = false) {
    try {
      Reader.read(programText) match {
        case Right(ToyList(forms)) => {
          for (form <- forms) {
            val result = Interpreter eval form
            if (quiet == false)
              println("result = " + result)
          }
        }
        case Left(msg) => throw SyntaxError(msg)
      }
    } catch {
      case ex => Console.err println (ex.getClass + ": " + ex.getMessage)
    }
  }
}
/* END MAIN */
