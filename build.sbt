scalaVersion := "3.10.0"

name := "beesim"
organization := "jackflashtech"
version := "1.0"

// Forced version upgrades in response to Dependabot alerts
val fasterxml3Version =
  "3.2.4" // Scaladoc 3.10.0 apparently has dependencies on both fasterxml jackson-core 3 and 2, which seems unwise, but the dependency on 3 is on 3.2.1 and the last LTS version of 3 is 3.1, so this is the latest release.
val fasterxml2Version = "2.21.8" // Latest patch of the most recent LTS.
val jsoupVersion = "1.24.1" // Latest patch. They don't seem to have a LTS policy, according to Google
val assertjVersion = "3.27.7"
val javafxVersion = "25.0.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test
libraryDependencies += "org.testfx" % "openjfx-monocle" % "21.0.2" % Test
libraryDependencies += "org.assertj" % "assertj-core" % assertjVersion % Test // Updated from 3.13.2 in response to Dependabot alert #30
// Would like to also remove media, I suspect, but can't because combo boxes use a class from that package.
libraryDependencies += "org.scalafx" %% "scalafx" % "25.0.2-R37" exclude ("org.openjfx", "javafx-web")
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % fasterxml2Version % Configurations.ScalaDocTool
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % fasterxml2Version % Configurations.ScalaDocTool
libraryDependencies += "org.jsoup" % "jsoup" % jsoupVersion % Configurations.ScalaDocTool
libraryDependencies += "tools.jackson.core" % "jackson-core" % fasterxml3Version % Configurations.ScalaDocTool
libraryDependencies += "tools.jackson.core" % "jackson-databind" % fasterxml3Version % Configurations.ScalaDocTool

// Drives the UI in tests the way a user does (dragging, clicking).
libraryDependencies += "org.testfx" % "testfx-core" % "4.0.18" % Test

// Adding all of the platforms into the classpath to create the assembly package. Not sure if this could be done only for packaging, rather than testing, but I'd prefer to do it after testing in case this disrupts the load order on different platforms or just bloats the dependencies on developer computers. Better to let the classifier system just resolve everything in those cases.
val javafxModules = Seq(
  "base",
  "controls",
  "fxml",
  "graphics",
  "media",
  "swing"
) // Removed "web" to avoid bloat. Would like to remove "media" as well, but comboboxes seem to need it.
val javafxPlatforms = Seq("mac-aarch64", "mac", "linux", "win") // "mac" = Intel Mac
libraryDependencies ++= (for {
  m <- javafxModules
  p <- javafxPlatforms
} yield "org.openjfx" % s"javafx-$m" % javafxVersion classifier p)

// This merge strategy was basically cut-and-paste from Google Search AI. It is to address issues of deduplication found when creating an fat assembly with JavaFX jars.
ThisBuild / assemblyMergeStrategy := {
  // Discard module-info.class files from modularized jars
  case PathList("module-info.class")                                 => MergeStrategy.discard
  case PathList("META-INF", "versions", xs @ _, "module-info.class") => MergeStrategy.discard

  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*)
      if xs.lastOption.exists(s => s.endsWith(".SF") || s.endsWith(".DSA") || s.endsWith(".RSA")) =>
    MergeStrategy.discard
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", xs @ _*)             => MergeStrategy.discard

  case x if x.endsWith(".bss")              => MergeStrategy.first
  case x if x.endsWith(".dylib")            => MergeStrategy.first
  case x if x.endsWith("VersionInfo.class") => MergeStrategy.first
  case "javafx-swt.jar"                     => MergeStrategy.first

  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
