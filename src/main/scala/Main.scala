package com.yuvimasory.toylisp

import java.io.{ File, OutputStreamWriter }

import scala.io.Source

import jline.{ ConsoleReader, History }

object Main {

  val in = {
    val consoleReader = new ConsoleReader(System.in,
                                          new OutputStreamWriter(System.out))
    consoleReader setHistory (new History(new File(".toyhistory")))
    consoleReader setUseHistory true
    consoleReader setDefaultPrompt ">> "
    consoleReader
  }

  val interpreter = new Interpreter()

  def main(args: Array[String]) {
    val stdlib = UrlReader resource2String "/stdlib.lis'"
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

  private def giveOutput(programText: String, quiet: Boolean = false) {
    try {
      Reader.read(programText) match {
        case Right(ToyList(forms)) => {
          for (form <- forms) {
            val result = interpreter interpret form
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

  private def simpleClass(arg: AnyRef): String =
    arg.getClass.toString.split("\\s+")(1).split("\\.").last
}

object URLReader {
  import java.io._
  import java.net._
    def resource2String(resource: String): String {
      val url = getClass getResource resource
      val in = new BufferedReader(
              new InputStreamReader(
                      url.openStream()));

      String inputLine;

      while ((inputLine = in.readLine()) != null)
          System.out.println(inputLine);

      in.close();
    }
}
