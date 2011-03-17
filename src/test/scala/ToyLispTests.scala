package com.yuvimasory.toylisp

import org.scalatest.FunSuite

class ToyLispTests extends FunSuite {

  test("dummy") {
    expect(1) {2-1}
    intercept[RuntimeException] {
      throw new RuntimeException
    }
  }

}
