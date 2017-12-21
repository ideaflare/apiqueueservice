import Dependencies._

lazy val akkaVersion = "2.5.8"
lazy val json4sVersion = "3.6.0-M2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    libraryDependencies += "org.json4s" % "json4s-native_2.12" % json4sVersion
  )
