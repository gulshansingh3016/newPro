ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .settings(
    name := "Projectknuron1"
  )
libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.6.17"

