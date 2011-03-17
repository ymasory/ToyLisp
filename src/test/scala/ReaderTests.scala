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
}
