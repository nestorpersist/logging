name := "persist-logging"

organization := "com.persist"

version := "1.2.1"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

viewSettings

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.2",
  "joda-time" % "joda-time" % "2.9.2",
  "ch.qos.logback" % "logback-classic"  % "1.1.6",
  "com.persist" % "persist-json_2.11" % "1.1.2")


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