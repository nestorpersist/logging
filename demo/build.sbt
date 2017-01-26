name := "demo"

organization := "com.persist"

version := "1.3.1"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

//viewSettings

/*
lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "logging_demo"
  )
*/  

//libraryDependencies ++= Seq(
//  "com.persist" % "persist-logging_2.12" % "1.3.1"
//)
