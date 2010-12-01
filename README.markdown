Toy Lisp interpreter in Scala
=============================
Build
-----
- Install simple-build-tool <http://code.google.com/p/simple-build-tool/>.
- `sbt proguard`
- `java -jar target/scala_2.8.1/ToyScala-*.min.jar`

Language
--------
# syntax #
- blah    = evaluated symbol
- (blah)  = function call, n-ary
- '(blah) = unevaluated list, the list is just data
- "blah"  = string (list of scala.Char)
- 3.14    = number (scala.Double)

# built-in #
- defn (named functions)
- set (assignment)
- lambda
- cons, head, tail
- char-to-num, num-to-char
- <=, +, floor, opp
- if
- eq
- print

# implementation #
- arbitrary nesting supported
- recursion supported
- no quoting mechanism beyond creating unevaluated lists using the ' syntax
- everything is in one big dynamic scope
- one namespace
- all evaluation is eager

