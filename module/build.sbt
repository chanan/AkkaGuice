name := "AkkaGuice"

version := "0.6.0-SNAPSHOT"

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

play.Project.playJavaSettings