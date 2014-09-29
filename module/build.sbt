name := "AkkaGuice"

version := "0.8.0"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "3.0",
  "org.reflections" % "reflections" % "0.9.9-RC1"
)

publishTo <<= version { (v: String) =>
	if (v.trim.endsWith("SNAPSHOT"))
    	Some(Resolver.file("file",  new File( "../../maven-repo/snapshots" )) )
    else
    	Some(Resolver.file("file",  new File( "../../maven-repo/releases" )) )
} 

publishArtifact in(Compile, packageDoc) := false

publishMavenStyle := true

scalaVersion := "2.11.1"

lazy val root = (project in file(".")).enablePlugins(PlayJava)