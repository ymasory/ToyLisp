Toy Lisp interpreter in Scala
=============================
Build
-----
Install sbt <http://code.google.com/p/simple-build-tool/>.
    sbt update
    sbt proguard
    java -jar target/scala_2.8.1/ToyLisp-*.min.jar

Language
--------
# syntax #
- foo    = evaluated symbol
- (foo)  = function call, n-ary
- '(foo) = unevaluated list, the list is just data
- "foo"  = string (list of characters)
- 'f'    = character (scala.Char)
- 3.14   = number (scala.Double)

# built-in #
- defn (named functions)
- set (assignment)
- lambda
- cons, head, tail
- cton, ntoc
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

