Toy Lisp interpreter in Scala
=============================
Build
-----
Install [sbt](http://code.google.com/p/simple-build-tool/).
    cd ToyLisp
    sbt update
    sbt proguard
    java -jar target/scala_2.8.1/ToyLisp-*.min.jar

Language
--------
# syntax #
- `foo`    (evaluated symbol)
- `(foo)`  (function call, n-ary)
- `[foo]`  (list)
- `"foo"`  (string (list of characters))
- `'c'`    (character)
- `3`      (integer)

# special forms #
- `set!`   (assignment)
- `lambda` (anonymous function)
- `if`     (conditional -- empty list and zero are falsy)
- `do`     (sequence of forms)

# built-in function #
- `cons`, `head`, `tail`
- `num>char`, `char>num`
- `<=`, `+`, `floor`, `opp`
- `eq?`
- `list?`, `char?`, `num?`

# scope #
- All variables are lexically scoped.
- All names live in the same namespace.

# standard library #
The standard library provides `-`, `*`, `**`.


