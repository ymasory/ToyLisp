Toy Lisp interpreter in Scala
=============================
Build
-----
Install sbt <http://code.google.com/p/simple-build-tool/>.
    cd ToyLisp
    sbt update
    sbt proguard
    java -jar target/scala_2.8.1/ToyLisp-*.min.jar

Language
--------
# syntax #
- foo    = evaluated symbol
- (foo)  = function call, n-ary
- [foo]  = list
- "foo"  = string (list of characters)
- 'f'    = character (scala.Char)
- 3.14   = number (scala.Double)

# special forms #
- defn   (named function)
- set!   (assignment)
- lambda (anonymous function)
- if     (conditional -- empty list and zero are falsy)
- do     (sequence of forms)

# built-in function #
- print
- cons, head, tail
- num>char, char>num
- <=, +, floor, opp
- eq?
- list?, char?, num?

# implementation #
- no quoting mechanism beyond creating unevaluated lists using the ' syntax
- everything is in one big dynamic scope, one namespace


