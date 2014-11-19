name := "AkkaGuiceSample"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "3.0"
)

lazy val module = (project in file("module")).enablePlugins(PlayJava)

lazy val root = (project in file(".")).enablePlugins(PlayJava).aggregate(module).dependsOn(module)

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")