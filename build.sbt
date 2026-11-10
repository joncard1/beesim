scalaVersion := "3.10.0"

name := "sim"
organization := "com.jackflashtech"
version := "1.0"

Compile / mainClass := Some("beesimulator.BeeSimulatorApp")

// Forced version upgrades in response to Dependabot alerts
val fasterxml3Version =
  "3.2.4" // Scaladoc 3.10.0 apparently has dependencies on both fasterxml jackson-core 3 and 2, which seems unwise, but the dependency on 3 is on 3.2.1 and the last LTS version of 3 is 3.1, so this is the latest release.
val fasterxml2Version = "2.21.8" // Latest patch of the most recent LTS.
val jsoupVersion = "1.24.1" // Latest patch. They don't seem to have a LTS policy, according to Google

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
libraryDependencies += "org.testfx" % "openjfx-monocle" % "21.0.2" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.27.7" % Test // Updated from 3.13.2 in response to Dependabot alert #30
libraryDependencies += "org.scalafx" %% "scalafx" % "25.0.2-R37"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % fasterxml2Version % Configurations.ScalaDocTool
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % fasterxml2Version % Configurations.ScalaDocTool
libraryDependencies += "org.jsoup" % "jsoup" % jsoupVersion % Configurations.ScalaDocTool
libraryDependencies += "tools.jackson.core" % "jackson-core" % fasterxml3Version % Configurations.ScalaDocTool
libraryDependencies += "tools.jackson.core" % "jackson-databind" % fasterxml3Version % Configurations.ScalaDocTool

// Drives the UI in tests the way a user does (dragging, clicking).
libraryDependencies += "org.testfx" % "testfx-core" % "4.0.18" % Test

ThisBuild / assemblyMergeStrategy := {
  case x if x.contains("reflectionconfig") => MergeStrategy.first
  case x if x.contains("jniconfig")        => MergeStrategy.first
  case x if x.contains("resourcebundles")  => MergeStrategy.first

  // Discard module-info.class files from modularized jars
  case PathList("module-info.class")                                 => MergeStrategy.discard
  case PathList("META-INF", "versions", xs @ _, "module-info.class") => MergeStrategy.discard

  // Handle other common conflicts if needed (e.g., JavaFX native or properties)
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.concat

  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
