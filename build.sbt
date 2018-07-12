organization := "com.yerlibilgin.minder"

name := "minder-tdl"

version := "1.1.0"

resolvers += Resolver.mavenLocal

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

javacOptions in (doc) ++= Seq("-source", "1.8")

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.yerlibilgin" % "yb-commons" % "1.0.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.0",
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang" % "scala-compiler" % "2.11.7",
  "com.typesafe" % "config" % "1.3.2",
  "com.yerlibilgin.minder" % "minder-common" % "1.1.0",
  "org.eclipse.aether" % "aether-api" % "1.1.0",
  "org.eclipse.aether" % "aether-spi" % "1.1.0",
  "org.eclipse.aether" % "aether-util" % "1.1.0",
  "org.eclipse.aether" % "aether-impl" % "1.1.0",
  "org.eclipse.aether" % "aether-connector-basic" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-classpath" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-file" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-http" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-wagon" % "1.1.0",
  "org.apache.maven" % "maven-aether-provider" % "3.3.9",
  "net.sf.saxon" % "Saxon-HE" % "9.6.0-3",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25" % Test,
  "junit" % "junit" % "4.12" % Test,
  "org.specs2" % "specs2_2.11" % "2.3.12" % Test
)

