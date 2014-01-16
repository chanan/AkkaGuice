name := "AkkaGuiceSample"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "3.0"
)     

play.Project.playJavaSettings

lazy val module = project.in(file("module"))

lazy val main = project.in(file(".")).dependsOn(module).aggregate(module)