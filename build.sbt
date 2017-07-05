import com.typesafe.sbt.SbtNativePackager.packageArchetype
import com.typesafe.sbt.packager.Keys._
import play.PlayImport.PlayKeys._

name := "miso-ramen"

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

libraryDependencies += "com.github.salat" %% "salat" % "1.10.0" 

libraryDependencies += "se.radley" %% "play-plugins-salat" % "1.5.0"

libraryDependencies += "org.mongodb" %% "casbah" % "2.8.2"

//libraryDependencies += "org.mongodb" %% "casbah" % "3.1.1"

//"com.beachape" %% "enumeratum-play" % "1.5.12" 


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}
