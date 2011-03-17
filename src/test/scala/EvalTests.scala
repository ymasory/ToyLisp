package com.yuvimasory.toylisp

import org.scalatest.FunSuite

//To reduce fragility these tests should really be on program text,
//not on ASTs.
class EvalTests extends FunSuite {
  import Interpreter.{EmptyList, EmptyEnvironment, eval}

  def evale(form: ToyForm): ToyForm = eval(form, EmptyEnvironment)._1


  test("eval int") {
    val ast = ToyInt(1)
    expect (ast) {
      evale(ast)
    }
  }

  test("eval char") {
    val ast = ToyChar('c')
    expect (ast) {
      evale(ast)
    }
  }

  test("eval list") {
    val ast = ToyList(List(ToyInt(1), ToyChar('c')))
    expect (ast) {
      evale(ast)
    }
  }

  test("unkown symbol") {
    intercept[UnboundSymbolError] {
      evale(ToySymbol("foo"))
    }
  }

  test("built-in +") {
    expect(ToyInt(3)) {
      evale(ToyCall(List(ToySymbol("+"), ToyInt(1), ToyInt(2))))
    }
  }

  test("built-in opp handles negatives") {
    expect(ToyInt(3)) {
      evale(ToyCall(List(ToySymbol("opp"), ToyInt(-3))))
    }
  }

  test("built-in opp handles positives") {
    expect(ToyInt(-3)) {
      evale(ToyCall(List(ToySymbol("opp"), ToyInt(3))))
    }
  }

  test("built-in opp handles 0") {
    expect(ToyInt(0)) {
      evale(ToyCall(List(ToySymbol("opp"), ToyInt(0))))
    }
  }

  test("assignment") {
    val ast = ToyCall(List(ToySymbol("set!"), ToySymbol("x"), ToyInt(1)))
    expect((EmptyList, Map(ToySymbol("x") -> ToyInt(1)))) {
      eval(ast, EmptyEnvironment)
    }
  }

  test("assignment requires a symbol and a form") {
    val ast = ToyCall(List(ToySymbol("set!"), ToyInt(1), ToyInt(1)))
    intercept[TypeError] {
      evale(ast)
    }
  }

  test("lambda plustwo") {
    val lambda = ToyLambda(
      List(ToySymbol("x")),
      ToyCall(List(
        ToySymbol("+"),
        ToySymbol("x"),
        ToyInt(2)))) 
    val ast = ToyCall(List(lambda, ToyInt(3)))
    expect(ToyInt(5)) {
      evale(ast)
    }
  }
}
