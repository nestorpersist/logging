name := "logging-demo"

organization := "com.persist"

version := "0.0.2"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-unchecked")

viewSettings

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "logging_demo"
  )

libraryDependencies ++= Seq(
  "com.persist" % "persist-logging_2.11" % "1.2.0"
)
