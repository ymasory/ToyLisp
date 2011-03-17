import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with ProguardProject with Exec {
  
  //project name
  override val artifactID = "ToyLisp"

  //managed dependencies from built-in repositories
  val scalatest = "org.scalatest" % "scalatest" % "1.3" % "test"

  //managed dependencies from external repositories
  val jline = "jline" % "jline" % "0.9.94"
  val sourceforgeJline = "http://jline.sourceforge.net/m2repo"

  //turn down logging a bit
  log setLevel Level.Warn
  log setTrace 2
  
  //files to go in packaged jars
  val extraResources = "README.md" +++ "LICENSE"
  override val mainResources = super.mainResources +++ extraResources

  //program entry point
  override def mainClass: Option[String] = Some("com.yuvimasory.toylisp.Main")

  //proguard: general options
  override def proguardOptions = List(
    "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
    proguardKeepAllScala
  )

  //proguard: remove jar signatures from jline or resulting jar will be invalid
  override def makeInJarFilter (jarPath: String) = {
    jarPath match {
      case _ => super.makeInJarFilter(jarPath) + ",!META-INF/.RSA,!META-INF/*.SF"
    }
  }

  //proguard: include scala-library.jar
  override def proguardInJars = Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}
