ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "Chatbot as a Service"
  )
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.7"
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.9.5",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.9.6",
  "dev.zio" %% "zio" % "1.0.17"
)
