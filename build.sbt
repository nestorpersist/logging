name := "logging"

scalaVersion := "2.12.1"

scalacOptions ++= Seq("-deprecation", "-unchecked")

lazy val logger = project

lazy val demo = project.dependsOn(logger)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "logging_demo"
  )

lazy val kafka = project

viewSettings



