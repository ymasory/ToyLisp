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
- `<=`, `+`, `opp`
- `eq?`
- `list?`, `char?`, `num?`

# deficiencies #
- Recursion is not supported. Since there are no loops either, ToyLisp is not Turing-complete.
- There's just one, horrifying, dynamic scope. `eval` should be revised to take an `Environment` argument.


