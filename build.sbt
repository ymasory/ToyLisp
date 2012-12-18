/* basic project info */
name := "ToyLisp"

organization := "com.yuvimasory"

version := "0.1.0-SNAPSHOT"

description := "Toy Lisp interpreter in Scala"

homepage := Some(url("https://github.com/ymasory/ToyLisp"))

startYear := Some(2012)

licenses := Seq(
  ("GPLv3", url("http://www.gnu.org/licenses/gpl-3.0.txt"))
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/ymasory/ToyLisp"),
    "scm:git:https://github.com/ymasory/ToyLisp.git",
    Some("scm:git:git@github.com:ymasory/ToyLisp.git")
  )
)

// organizationName := "My Company"

/* scala versions and options */
scalaVersion := "2.9.2"

offline := false

scalacOptions ++= Seq("-deprecation", "-unchecked")

javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

/* entry point */
mainClass in (Compile, packageBin) := Some("com.yuvimasory.toylisp.Main")

mainClass in (Compile, run) := Some("com.yuvimasory.toylisp.Main")

/* dependencies */
libraryDependencies ++= Seq (
  "jline" % "jline" % "0.9.94",
  "org.scalatest" %% "scalatest" % "1.8" % "test"
)

/* testing */
parallelExecution in Test := false

// testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "junitxml")

// parallelExecution in Global := false //no parallelism between subprojects

/* sbt behavior */
logLevel in compile := Level.Warn

traceLevel := 5
