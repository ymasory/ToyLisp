package com.yuvimasory.toylisp

import java.io.OutputStreamWriter

import jline.ConsoleReader

object Main {

  val in = new ConsoleReader(System.in, new OutputStreamWriter(System.out))
  
  def main(args: Array[String]) {
    println("Welcome to Toy Lisp! Press Ctrl+D to exit.\n")
    while(true) {
      in.readLine(">> ") match {
        case input: String => println(input)
        case _ => {
          println("okbye!")
          return
        }
      }
    }
  }
}
