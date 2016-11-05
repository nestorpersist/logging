name := "kafka-appender"

organization := "com.persist"

version := "1.3.0"

scalaVersion := "2.12.0"

scalacOptions ++= Seq("-deprecation", "-unchecked")

viewSettings

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "kafka_logging_demo"
  )

libraryDependencies ++= Seq(
   "com.persist" % "persist-logging_2.12" % "1.3.0",
   "org.apache.kafka" % "kafka-clients" % "0.10.1.0"
)

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

homepage := Some(url("https://github.com/nestorpersist/logging"))

scmInfo := Some(ScmInfo(url("https://github.com/nestorpersist/logging"), "scm:git@github.com:nestorpersist/logging"))

pomExtra := (
  <developers>
    <developer>
      <id>johnnestor</id>
      <name>John Nestor</name>
      <email>nestor@persist.com</email>
      <url>http://http://www.persist.com</url>
    </developer>
  </developers>
  )
