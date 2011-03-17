package com.yuvimasory.toylisp

import org.scalatest.FunSuite

//To reduce fragility these tests should really be on program text,
//not on ASTs.
class EvalTests extends FunSuite {
  import Interpreter.{EmptyList, EmptyEnvironment, eval}

  def evale(form: ToyForm): ToyForm = eval(form, EmptyEnvironment)._1
  def run(text: String) = Reader.read(text) match {
    case Right(form) => evale(form)
    case Left(msg) => throw SyntaxError(msg)
  }
  def textTest(text: String, form: ToyForm) {
    expect(form) {
      run(text).asInstanceOf[ToyList].lst.last
    }
  }
  val Dummy = EmptyList

  test("eval int") {
    textTest("1", ToyInt(1))
  }

  test("eval char") {
    textTest("'c'", ToyChar('c'))
  }

  test("eval list") {
    val ast = ToyList(List(ToyInt(1), ToyChar('c')))
    textTest("[1 'c']", ast)
  }

  test("unkown symbol") {
    intercept[UnboundSymbolError] {
      evale(ToySymbol("foo"))
    }
  }

  test("built-in +") {
    textTest("(+ 1 2)", ToyInt(3))
  }

  test("built-in opp handles negatives") {
    textTest("(opp -3)", ToyInt(3))
  }

  test("built-in opp handles positives") {
    textTest("(opp 3)", ToyInt(-3))
  }

  test("built-in opp handles zero") {
    textTest("(opp 0)", ToyInt(0))
  }

  test("assignment") {
    pending
    textTest("(set! x 5) x", ToyInt(5))
  }

  test("assignment requires a symbol and a form") {
    intercept[TypeError] {
      textTest("(set! 1 3)", Dummy)
    }
  }

  test("lambda plustwo") {
    textTest("((lambda [x] (+ x 2)) 3)", ToyInt(5))
  }

  test("PHASE example") {
    pending
    textTest(Common.phaseProgram, ToyInt(-5))
  }
}
