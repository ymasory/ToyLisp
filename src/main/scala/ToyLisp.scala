package com.yuvimasory.toylisp

import java.io.{ File, OutputStreamWriter }

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

  lazy val QuoteStr: String = "'"

  //handy string/regex parsers
  lazy val lParen: Parser[String] = "("
  lazy val rParen: Parser[String] = ")"
  lazy val lBrack: Parser[String] = "["
  lazy val rBrack: Parser[String] = "]"
  lazy val quote: Parser[String] = QuoteStr

  //parsers for "primitive types"
  lazy val toySymbol: Parser[ToySymbol] =
    """[a-zA-Z_@~%!=#<>\-\+\*\?\^\&]+""".r ^^ { ToySymbol(_) }
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
  lazy val toyListOfSymbols: Parser[List[ToySymbol]] =
    lBrack ~> (toySymbol+) <~ rBrack
  lazy val toyLambda: Parser[ToyLambda] =
    lParen ~> "lambda".r ~> toyListOfSymbols ~ toyCall <~ rParen ^^ {
      case lst ~ call => ToyLambda(lst, call)
    }

  //"primitive types", list types, and sugar types together make all the forms
  //notice that toyLambda MUST come earlier in the chain than toyCall
  lazy val toyForm: Parser[ToyForm] =
    toyChar | toyInt | toyString | toySymbol | toyList |  toyLambda | toyCall

  //in the tradition of Lisp, a program is a list of forms
  lazy val toyProgram: Parser[ToyList] = (toyForm*) ^^ { ToyList(_) }
}

// Algebraic data types for target language terms
sealed abstract class ToyForm
case class ToyChar(chr: Char) extends ToyForm
case class ToyInt(i: Int) extends ToyForm
case class ToyList(lst: List[ToyForm]) extends ToyForm
case class ToySymbol(str: String) extends ToyForm
sealed abstract class AbstractToyCall extends ToyForm
case class ToyCall(lst: List[ToyForm]) extends AbstractToyCall
case class ToyLambda(
  args: List[ToySymbol],
  body: ToyCall) extends AbstractToyCall //the body really should be any ToyForm

case class SyntaxError(msg: String) extends RuntimeException(msg)
/* END PARSER */

/* BEGIN INTERPRETER */
object Interpreter {
  
  //AKA "symbol table"
  type Environment = Map[ToySymbol, ToyForm]
  val EmptyEnvironment: Environment = Map.empty[ToySymbol, ToyForm]

  //the empty list is our "boring" unit value
  val EmptyList = ToyList(Nil)

  // def eval(lst: ToyList): (ToyForm, Environment) = {
  //   val res = lst match {
  //     case ToyList(lst) => {
  //       lst.foldLeft((EmptyEnvironment, EmptyList.asInstanceOf[ToyForm])) {
  //         case ((lastVal: ToyForm, env: Environment), form: ToyForm) => {
  //           eval(form, env).swap
  //         }
  //       }
  //     }
  //   }
  //   res.swap
  // }

  def eval(form: ToyForm, env: Environment): (ToyForm, Environment) = {
    def lookup(symb: ToySymbol) =
      env getOrElse (symb, throw UnboundSymbolError(symb.toString))

    form match {
      case ToyChar(_) | ToyInt(_) => (form, env)
      case ToyList(lst) =>
        (ToyList(lst.map(eval(_, env)).unzip._1), env)
      case ToyLambda(_, _) => (form, env) //evaluation of lambda body is delayed
      case symb: ToySymbol => (lookup(symb), env)
      case call: ToyCall => functionApplication(call, env)
    }
  }

  def functionApplication(toyCall: ToyCall, env: Environment):
    (ToyForm, Environment) = {
      def evale(form: ToyForm): ToyForm = eval(form, env)._1

      (toyCall: @unchecked) match {

        case ToyCall(firstForm :: restForms) => firstForm match {
          case ToySymbol("+") => (restForms map evale) match {
            case List(ToyInt(a), ToyInt(b)) => (ToyInt(a + b), env)
            case _ => throw TypeError("plus needs exactly two numbers")
          }
          case ToySymbol("opp") => (restForms map evale) match {
            case List(ToyInt(a)) => (ToyInt(-a), env)
            case _ => throw TypeError("opp needs one number")
          }
          case ToySymbol("set!") => restForms match {
            case List(s: ToySymbol, form) =>
              (EmptyList, env + (s -> evale(form)))
            case _ => throw TypeError("set needs a symbol and a form")
          }
          case tl: ToyLambda => lambdaApplication(tl, restForms, env)

          case form => {
            evale(form) match {
              case tl: ToyLambda => lambdaApplication(tl, restForms, env)
              case _ => {
                throw TypeError(
                  "first element of a function call" +
                  " must result in a lambda or be a built-in function")
              }
            }
          }
        }
      }
    }


  private def lambdaApplication(lambda: ToyLambda,
                                forms: List[ToyForm],
                                env: Environment): (ToyForm, Environment) = {
      lambda match {
        case ToyLambda(args, body) => {
          if (args.length == forms.length) {
            val envWithParams = (0 until args.length).foldLeft(EmptyEnvironment)
            {
                case (env, i) => env + (args(i) -> forms(i))
            }
            eval(body, envWithParams)
          }
          else
            throw TypeError("tried to call a lambda with wrong number of args")
        }
      }
  }

}

case class UnboundSymbolError(msg: String) extends RuntimeException(msg)
case class TypeError(msg: String) extends RuntimeException(msg)
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
        case input: String => interpret(input)
        case _ => return println("okbye!")
      }
    }
  }

  /**
   * Interpret the programText, then if `quiet` is `false` prettily display
   * the output.
   */
  private def interpret(programText: String, quiet: Boolean = false) {
    try {
      Reader.read(programText) match {
        case Right(ToyList(forms)) => {
          var curEnv = Interpreter.EmptyEnvironment
          for (form <- forms) {
            val result = Interpreter eval (form, Interpreter.EmptyEnvironment)
            if (quiet == false)
              println("result = " + result._1)
          }
        }
        case Left(msg) => throw SyntaxError(msg)
      }
    } catch {
      case ex =>
        Console.err println (ex.getClass.getSimpleName + ": " + ex.getMessage)
    }
  }
}
/* END MAIN */
