# Intro #
## Where to find stuff ##
- Yuvi Masory
- http://yuvimasory.com
- ymasory@gmail.com
- https://github.com/ymasory/ToyLisp (code)
- https://github.com/ymasory/ToyLisp/README.md (language handout)
- https://github.com/ymasory/ToyLisp/PHASE-outline.md (talk outline handout)

## My goals ##
- **Break the barrier!** *language user -> language tinkerer*
- Give an intro to the parser combinators API.
- Give an intro to evaluation.

## Audience background ##
- Who is comfortable with basic regular expressions?
- Who could sit down and right an interpreter?
- Who could sit down and write a compiler?

# Overview of Lisp #
## Why Lisp? ##
- Lisp is easy to parse.
- Lisp is easy to evaluate.
- I'm not a Lisper.

## ToyLisp ##
### Available forms ###
- Simple literals: `"a string"`, `'c'`, `3`
- List: `[1 2 3]`
- Function call: `(+ 2 2)`
- Lambda: `(lambda [x] (* x 2))`
- Assignment: `(set! timestwo (lambda [x] (* x 2)))`

### Our goal ###
We want to interpret this program:

    (set! -
          (lambda [x y]
            (+ x (opp y))))

    (- 10 15)


# Overview of Interpretation #
## Conceptual overview ##
1. Sequence of characters -> tokens. (*lexing*).
2. Sequence of tokens -> parse trees. (*parsing*).
3. Parse trees -> abstract syntax trees (AST).
4. AST -> value. (evaluation).

A *value* is an expression that cannot be evaluated any further.

The parser (lisp-speak: the `reader`) does 1-3. The evaluator (lisp-speak: the `eval` function) does 4.

## Scala overview ##
- `main` is a procedure that will read line after line of input from the user, interactively.
- `main` feeds source text the `read` function of type `String => ToyList`.
- `main` gives the `ToyList`, along with an empty `Environment`, to `eval` which is of type `(ToyList, Environment) -> ToyForm`. 
- `main` them prints the resulting `ToyForm` value.

# Parsing #

# Evaluation #
