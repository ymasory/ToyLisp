package com.yuvimasory.toylisp

import java.io.OutputStreamWriter

import scala.io.Source

import jline.ConsoleReader

object Main {

  val Version = "0.1"

  val in = new ConsoleReader(System.in, new OutputStreamWriter(System.out))
  
  def main(args: Array[String]) {
    if(args.length > 0) runFile(args(0))
    else runInteractive()
  }

  def runFile(path: String) {
      val programText = Source.fromFile(path).mkString
      Reader.read(programText) match {
        case Some(form) => println(new Interpreter().interpret(form))
        case None => println("syntax error")
      }
  }

  def runInteractive() {
    val interpreter = new Interpreter()
    println("\nWelcome to Toy Lisp v" + Version + "! Press Ctrl+D to exit.\n")
    while(true) {
      in.readLine(">> ") match {
        case input: String => {
          Reader.read(input) match {
            case Some(form) => println(interpreter.interpret(form))
            case None => println("syntax error")
          }
        }
        case _ => {
          println("okbye!")
          return
        }
      }
    }
  }
}
