package com.yuvimasory.toylisp

import org.scalatest.FunSuite

class ReaderTests extends FunSuite {
  import Reader._

  def parseShouldFail[X](parser: Parser[X], input: String) = {
    parseAll(parser, input) match {
      case Success(_, _) => fail("parse should fail")
      case NoSuccess(_, _) =>
    }
  }

  test("trivial parsers accept") {
    expect("(") {
      parseAll(lParen, "(").get
    }
  }

  test("trivial parsers reject") {
    parseShouldFail(lParen, ")")
  }

  test("toySymbol accepts operators") {
    expect(ToySymbol("!")) {
      parseAll(toySymbol, "!").get
    }
  }

  test("toySymbol rejects brackets") {
    parseShouldFail(toySymbol, "]")
  }


  test("toyChar accepts alphabetic characters") {
    expect(ToyChar('m')) {
      parseAll(toyChar, "'m'").get
    }
  }
  test("toyChar accepts period") {
    expect(ToyChar('.')) {
      parseAll(toyChar, "'.'").get
    }
  }

  test("toyChar accepts space character") {
    pending
    expect(ToyChar(' ')) {
      parseAll(toyChar, "' '").get
    }
  }

  test("toyChar rejects multiple characters") {
    parseShouldFail(toyChar, "'mm'")
  }
  test("toyChar knows the empty character doesn't exist") {
    parseShouldFail(toyChar, "''")
  }

  test("toyInt accepts positive numbers") {
    expect(ToyInt(51)) {
      parseAll(toyInt, "51").get
    }
  }

  test("toyInt accepts negative numbers") {
    expect(ToyInt(-10)) {
      parseAll(toyInt, "-10").get
    }
  }

  test("toyInt accepts zero") {
    expect(ToyInt(0)) {
      parseAll(toyInt, "0").get
    }
  }

  test("toyList accepts singleton") {
    expect(ToyList(List(ToyInt(1)))) {
      parseAll(toyList, "[1]").get
    }
  }

  test("toyList accepts multiples") {
    expect(ToyList(List(ToyInt(1), ToyInt(2)))) {
      parseAll(toyList, "[1 2]").get
    }
  }

  test("toyList accepts whitespace") {
    expect(ToyList(List(ToyInt(1)))) {
      parseAll(toyList, "[ 1 ]").get
    }
  }

  test("toyCall rejects singletons") {
    pending
    parseShouldFail(toyCall, "(foo)")
  }

  test("toyCall accepts multiples") {
    expect(ToyCall(List(ToySymbol("foo"), ToyInt(2)))) {
      parseAll(toyCall, "(foo 2)").get
    }
  }

  test("toyCall accepts whitespace") {
    expect(ToyCall(List(ToySymbol("foo"), ToyInt(1)))) {
      parseAll(toyCall, "( foo  1 )").get
    }
  }

  test("empty string is not a ToyForm") {
    parseShouldFail(toyForm, "")
  }

  test("empty space is not a ToyForm") {
    parseShouldFail(toyForm, " ")
  }

  test("toyListOfSymbols accepts") {
    expect(ToyList(List(ToySymbol("foo")))) {
      parseAll(toyListOfSymbols, "[foo]").get
    }
  }

  test("toyListOfSymbols rejects non-symbols") {
    parseShouldFail(toyListOfSymbols, "[(+ 3 4)]")
  }
}
