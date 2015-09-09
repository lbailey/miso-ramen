import com.typesafe.sbt.SbtNativePackager.packageArchetype
import com.typesafe.sbt.packager.Keys._
import play.PlayImport.PlayKeys._

name := "go-be"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

routesImport += "se.radley.plugin.salat.Binders._"

//templatesImport += "org.bson.types.ObjectId"

//resolvers ++= Seq(Resolvers.sonatype, Resolvers.scalaSbt)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )

libraryDependencies += "com.novus" %% "salat" % "1.9.9"

libraryDependencies += "se.radley" %% "play-plugins-salat" % "1.5.0"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}
