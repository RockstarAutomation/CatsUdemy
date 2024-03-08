ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .settings(
    name := "CatsUdemy"
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.1.1")
