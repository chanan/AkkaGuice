name := "AkkaGuice"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "3.0",
  "org.reflections" % "reflections" % "0.9.9-RC1"
)     

play.Project.playJavaSettings