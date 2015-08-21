organization := "gov.tubitak.minder"

name := "minder-tdl"

version := "0.2.4"

resolvers += Resolver.mavenLocal

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")


scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.4",
  "org.scala-lang" % "scala-reflect" % "2.11.4",
  "org.scala-lang" % "scala-actors" % "2.11.4",
  "org.scala-lang" % "scala-compiler" % "2.11.4",
  "gov.tubitak.minder" % "minder-common" % "0.2.2",
  "org.specs2" % "specs2_2.11" % "2.3.11" % "test",
  "org.eclipse.aether" % "aether-api" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-spi" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-util" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-impl" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-connector-basic" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-classpath" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-file" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-http" % "1.0.2.v20150114",
  "org.eclipse.aether" % "aether-transport-wagon" % "1.0.2.v20150114",
  "org.apache.maven" % "maven-aether-provider" % "3.1.0"
)

publishTo := Some("eid releases" at "http://eidrepo:8081/nexus/content/repositories/releases")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

