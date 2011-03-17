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
    expect(ToyList(List(form))) {
      run(text)
    }
  }

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

  // test("assignment") {
  //   val ast = ToyCall(List(ToySymbol("set!"), ToySymbol("x"), ToyInt(1)))
  //   expect((EmptyList, Map(ToySymbol("x") -> ToyInt(1)))) {
  //     eval(ast, EmptyEnvironment)
  //   }
  // }

  // test("assignment requires a symbol and a form") {
  //   val ast = ToyCall(List(ToySymbol("set!"), ToyInt(1), ToyInt(1)))
  //   intercept[TypeError] {
  //     evale(ast)
  //   }
  // }

  // test("lambda plustwo") {
  //   val lambda = ToyLambda(
  //     List(ToySymbol("x")),
  //     ToyCall(List(
  //       ToySymbol("+"),
  //       ToySymbol("x"),
  //       ToyInt(2)))) 
  //   val ast = ToyCall(List(lambda, ToyInt(3)))
  //   expect(ToyInt(5)) {
  //     evale(ast)
  //   }
  // }
}
