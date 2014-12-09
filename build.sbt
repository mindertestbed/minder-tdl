organization := "gov.tubitak.minder"

name := "minder-tdl"

version := "0.0.1"

resolvers += Resolver.mavenLocal

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang" % "scala-compiler" % "2.11.4",
  "gov.tubitak.minder" % "minder-common" % "0.0.2",
  "org.specs2" % "specs2_2.11" % "2.3.11" % "test"
)
