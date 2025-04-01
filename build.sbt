ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "SparkProject",
    Compile / unmanagedJars ++= Seq(
      baseDirectory.value / "lib" / "google-api-services-drive-v3-rev20241027-2.0.0.jar",
      baseDirectory.value / "lib" / "google-http-client-jackson2-1.42.3.jar"
    ),
    libraryDependencies ++= Seq(
      "com.google.api-client" % "google-api-client" % "1.34.0",
      "com.google.oauth-client" % "google-oauth-client-jetty" % "1.36.0",
      "org.slf4j" % "slf4j-nop" % "1.7.36",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.13.5", // Obezbeđuje JsonFactory
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.13.5",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.5"
    )
  )
