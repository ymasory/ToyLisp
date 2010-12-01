import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with ProguardProject with Exec {
  
  //project name
  override val artifactID = "ToyScala"

  //managed dependencies from externa repositories
  val jline = "jline" % "jline" % "0.9.5"
  val sourceforgeJline = "http://jline.sourceforge.net/m2repo"
  
  //files to go in packaged jars
  val extraResources = "README.markdown" +++ "LICENSE"
  override val mainResources = super.mainResources +++ extraResources

  //program entry point
  override def mainClass: Option[String] = Some("com.yuvimasory.toylisp.Main")

  //proguard
  override def proguardOptions = List(
    "-keepclasseswithmembers public class * { public static void main(java.lang.String[]); }",
    "-dontoptimize",
    "-dontobfuscate",
    proguardKeepLimitedSerializability,
    proguardKeepAllScala,
    "-keep interface scala.ScalaObject"
  )
  override def proguardInJars = Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}
