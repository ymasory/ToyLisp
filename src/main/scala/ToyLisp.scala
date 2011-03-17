package com.yuvimasory.toylisp

import java.io.{ BufferedReader, File, InputStreamReader, OutputStreamWriter }

import scala.collection.{ mutable => m }
import scala.io.Source
import scala.util.parsing.combinator.{ RegexParsers, JavaTokenParsers }

import jline.{ ConsoleReader, History }

/* BEGIN PARSER */
object Reader {

  def read(programText: String): Either[String, ToyList] = {
    import ToyParsers.{ toyProgram, parseAll, NoSuccess, Success }
    parseAll(toyProgram, programText) match {
      case Success(form, _) => Right(recognizeSpecialForms(form))
      case NoSuccess(msg, _) => Left(msg)
    }
  }

  private[toylisp] object ToyParsers extends RegexParsers with JavaTokenParsers {

    //in the tradition of Lisp, a program is a list of forms
    lazy val toyProgram: Parser[ToyList] =
      (((ws*) ~> toyForm <~ (ws*))*) ^^ { ToyList(_) }

    //we will handle whitepsace ourselves
    override val skipWhitespace = false

    lazy val quoteStr: String = "'"

    //handy string/regex parsers
    lazy val lParen: Parser[String] = "("
    lazy val rParen: Parser[String] = ")"
    lazy val lBrack: Parser[String] = "["
    lazy val rBrack: Parser[String] = "]"
    lazy val quote: Parser[String] = quoteStr
    lazy val ws: Parser[String] = """\s+""".r

    //"primitive types" parsers
    lazy val toySymbol: Parser[ToySymbol] =
      """[a-zA-Z_@~%!=#<>\+\*\?\^\&]+""".r ^^ { ToySymbol(_) }
    lazy val toyChar: Parser[ToyChar] =
      quote ~> "[^.]".r <~ quote ^^ { s => ToyChar(s charAt 0) }
    lazy val toyNumber: Parser[ToyInt] = floatingPointNumber ^^ { str =>
      ToyInt(str.toDouble)
    }

    //syntactic sugar parsers
    lazy val toyString: Parser[ToyCall] = stringLiteral ^^ { str =>
      {
        val chars =
          str.substring(1, str.length - 1).toList.map(quoteStr + _ + quoteStr)
        val sExpr = "(" + chars.mkString(" ") + ")"
        parse(toyCall, sExpr).get
      }
    }

    //list types parser
    lazy val toyCall: Parser[ToyCall] =
      lParen ~> (((ws*) ~> toyForm <~ (ws*))*) <~ rParen ^^ { ToyCall(_) }
    lazy val toyList: Parser[ToyList] =
      lBrack ~> (((ws*) ~> toyForm <~ (ws*))*) <~ rBrack ^^ { ToyList(_) }

    //"primitive types", list types, and sugar types together make all the forms
    lazy val toyForm: Parser[ToyForm] =
      toySymbol | toyNumber | toyChar | toyCall | toyList | toyString
  }

  def isSymbol(form: ToyForm): Boolean = {
    form match {
      case ToySymbol(_) => true
      case _ => false
    }
  }

  def recognizeSpecialForms(form: ToyForm): ToyList = {
    val m = form match {
      case ToyLambda(_, _) | ToyDo(_) => form // eliminate, unreachable
      case ToyList(q) => ToyList(q map recognizeSpecialForms)
      case ToyCall(List(ToySymbol("lambda"),
        ToyCall(args),
        body)) =>
        if (args.forall(a => isSymbol(a))) {
          ToyLambda((for (ToySymbol(a) <- args) yield ToySymbol(a)).toList,
            recognizeSpecialForms(body))
        } else {
          throw SyntaxError("lambda requires only symbol names in arg list")
        }
      case ToyCall(ToySymbol("do") :: stmts) => ToyDo(stmts)
      case ToyChar(_) | ToyInt(_) | ToySymbol(_) => form
      case ToyCall(forms) => ToyCall(forms map recognizeSpecialForms)
    }
    ToyList(List(m))
  }
}

// Algebraic data types for target language terms
sealed abstract class ToyForm
case class ToyDo(stmts: List[ToyForm]) extends ToyForm
case class ToyLambda(args: List[ToySymbol], body: ToyForm) extends ToyForm
case class ToyChar(chr: Char) extends ToyForm
case class ToyInt(dub: Double) extends ToyForm
case class ToySymbol(str: String) extends ToyForm
case class ToyCall(lst: List[ToyForm]) extends ToyForm
case class ToyList(lst: List[ToyForm]) extends ToyForm

/* END PARSER */

/* BEGIN INTERPRETER */
object Interpreter {

  def interpret(form: ToyForm): ToyForm = {
    form match {
      case ToyDo(stmts) => stmts.foldLeft(
        emptyList.asInstanceOf[ToyForm]) { (_, form) =>
          interpret(form)
        }
      case ToyLambda(_, _) => form
      case ToyChar(_) | ToyInt(_) | ToyList(_) => form
      case symb: ToySymbol => lookupSymbol(symb)
      case lst: ToyCall => functionApplication(lst)
    }
  }

  private val one = ToyInt(1.0)
  private val zero = ToyInt(0.0)
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
    val form = environment getOrElse (symb,
      throw UnboundSymbolError(symb.toString))
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
        } else
          throw SyntaxError("tried to call a lambda with wrong number of args")
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
          case List(ToyInt(_)) => one
          case _ => zero
        }
        case ToySymbol("eq?") => (restForms map interpret) match {
          case List(ToyInt(a), ToyInt(b)) => if (a == b) one else zero
          case List(ToyChar(a), ToyChar(b)) => if (a == b) one else zero
          case List(ToyList(a), ToyList(b)) => if (a == b) one else zero
          case _ => zero
        }
        case ToySymbol("char>num") => (restForms map interpret) match {
          case List(ToyChar(c)) => ToyInt(c.toInt)
          case _ => throw SyntaxError("char>num needs one char")
        }
        case ToySymbol("num>char") => (restForms map interpret) match {
          case List(ToyInt(n)) => ToyChar(n.toChar)
          case _ => throw SyntaxError("num>char needs one number")
        }
        case ToySymbol("+") => (restForms map interpret) match {
          case List(ToyInt(a), ToyInt(b)) => ToyInt(a + b)
          case _ => throw SyntaxError("plus needs two numbers")
        }
        case ToySymbol("opp") => (restForms map interpret) match {
          case List(ToyInt(a)) => ToyInt(-a)
          case _ => throw SyntaxError("opp needs one number")
        }
        case ToySymbol("<=") => (restForms map interpret) match {
          case List(ToyInt(a), ToyInt(b)) => if (a <= b) one else zero
          case _ => throw SyntaxError("plus needs two numbers")
        }
        case ToySymbol("floor") => (restForms map interpret) match {
          case List(ToyInt(a)) => ToyInt(java.lang.Math.floor(a))
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
          case List(cond, ift, iff) => interpret(if (falsy(interpret(cond))) ift
          else iff)
          case _ => throw SyntaxError("if requires three arguments")
        }
        case userFunc => {
          interpret(userFunc) match {
            case tl: ToyLambda => handleLambda(tl, restForms)
            case _ => throw SyntaxError("first element of a function call must" +
              " be the lambda keyword or result in a lambda")
          }
        }
      }
      case _ => throw SyntaxError("use [] for empty list")
    }
  }
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
    val stdlib = ResourceReader resource2String "/stdlib.lis'"
    println("stdlib: " + stdlib)
    if (args.length > 0) runFile(args(0))
    else runInteractive()
  }

  def runFile(path: String, quiet: Boolean = false) {
    val programText = Source.fromFile(path).mkString
    giveOutput(programText, quiet)
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
            val result = Interpreter interpret form
            if (quiet == false)
              println(simpleClass(result) + " = " + result)
          }
        }
        case Left(msg) => throw SyntaxError(msg)
      }
    } catch {
      case ex => Console.err println (simpleClass(ex) + ": " + ex.getMessage)
    }
  }

  /** Convert a fully qualified class name into a bare class name. */
  private def simpleClass(arg: AnyRef): String =
    arg.getClass.toString.split("\\s+")(1).split("\\.").last

  object ResourceReader {

    /**
     * Dump the contents of the resource with the provided name into a `String`.
     */
    def resource2String(resource: String): String = {
      val url = getClass getResource resource
      val in = new BufferedReader(new InputStreamReader(url.openStream()))
      var buff = new StringBuffer
      var inputLine: String = in readLine ()
      while (inputLine != null) {
        buff append inputLine
        System.out.println(inputLine)
        inputLine = in readLine ()
      }
      in close ()
      buff toString
    }
  }
}
/* END MAIN */
