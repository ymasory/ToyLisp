# Intro #
## Stuff ##
- Yuvi Masory
- http://yuvimasory.com
- ymasory@gmail.com

- https://github.com/ymasory/ToyLisp (code)
- https://github.com/ymasory/ToyLisp/README.md (language handout)
- https://github.com/ymasory/ToyLisp/PHASE-outline.md (talk outline handout)

## My goal ##
- **Break the barrier!** *language user -> language tinkerer*
- Give an intro to the parser combinators API.
- Give an intro to evaluation.
- Demo a common workflow.

## Audience background ##
- Who is comfortable with basic regular expressions?
- Who could sit down and right an interpreter?
- Who could sit down and write a compiler?
- Who uses sbt? Emacs? ENSIME? GitHub?

# Overview of Lisp #
## Why Lisp? ##
- Lisp is easy to parse.
- Lisp is easy to evaluate.
- I'm not a Lisper.

## What is Lisp anyway? ##
Adapting from [Paul Graham](http://www.paulgraham.com/icad.html):

1. fully parenthesized prefix notation
2. conditionals
3. lambdas and first class functions
4. recursion
5. dynamic typing
6. garbage collection
7. programs as trees of expressions
8. symbol type
9. notation for code using trees of symbols and constants
10. minimal distinction between read-time and run-time, including macros

ToyLisp supports supports 1-7.

# Setup #
## IDEs ##
- Emacs + scala-mode + sbt (+ ENSIME). I think this is the "premier" environment.
- IntelliJ Idea with Scala plugin. Has some fans. Scala plugin is first-party. Integration with sbt is possible.
- Eclipse with Scala plugin. Not worth using, but may be after Scala v2.9.
- Netbeans with Scala plugin. Not worth using.
- I don't know about other text editors (vim, TextMage, etc)

## My workflow ##
- Make GitHub project.
- Make sbt project.
- Add other sbt tweaks.
- Add Proguard to sbt.
- Make ENSIME project.
- Add `Main` object.
- Add a test class.

# Interpreters #
1. Sequence of characters -> tokens. (*lexing*).
2. Sequence of tokens -> parse trees. (*parsing*).
3. Parse trees -> abstract syntax trees (AST).
4. AST -> intermediate representation (IR).
5. IR-1 -> ... -> IR-n.
6. Final IR -> value. (evaluation).


# Parsing #
## Overview of Parser ##
- Main runs stdlib, then runs file or goes into interactive mode.
- Reader should be of type `String -> ToyList`.
- Eval should be of type `(ToyList, Environment) -> ToyForm`.

# Evaluation #
