package com.yuvimasory.toylisp

import org.scalatest.FunSuite

class EvalTests extends FunSuite {

  def evale(form: ToyForm): ToyForm =
    Interpreter.eval(form, Interpreter.EmptyEnvironment)._1


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

}
