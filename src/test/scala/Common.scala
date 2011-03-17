package com.yuvimasory.toylisp

object Common {
  val phaseProgram =
"""
(set! minus
      (lambda [x y]
        (+ x (opp y))))

(minus 10 15)
"""
}
