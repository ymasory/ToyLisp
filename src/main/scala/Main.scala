package com.yuvimasory.toylisp

import java.io.{File, OutputStreamWriter}

import scala.io.Source

import jline.{ConsoleReader, History}

object Main {

  val Version = "0.1"

  val in = {
    val consoleReader = new ConsoleReader(System.in, new OutputStreamWriter(System.out))
    consoleReader setHistory (new History(new File(".toyhistory")))
    consoleReader setUseHistory true
    consoleReader setDefaultPrompt ">> "
    consoleReader
  }
  val interpreter = new Interpreter()

  def main(args: Array[String]) {
    if(args.length > 0) runFile(args(0))
    else runInteractive()
  }

  def runFile(path: String) {
    val programText = Source.fromFile(path).mkString
    giveOutput(programText)
  }

  def runInteractive() {
    println("\nWelcome to Toy Lisp v" + Version + "! Press Ctrl+D to exit.\n")
    while(true) {
      in.readLine() match {
        case input: String => giveOutput(input)
        case _ => return println("okbye!")
      }
    }
  }

  private def giveOutput(programText: String) {
    Reader.read(programText) match {
      case Some(listForms) => {
        for (form <- listForms.lst) {
          val result = interpreter.interpret(form)
          val resType = result.getClass.toString.split("\\s+")(1).split("\\.").last
          println(resType + " = " + result)
        }
      }
      case None => println("syntax error")
    }
  }
}
