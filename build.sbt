organization in ThisBuild := "com.marchanka"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.13.0"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.1.1" % Test

lazy val `lobby` = (project in file("."))
  .aggregate(`lobby-api`, `lobby-impl`)

lazy val `lobby-api` = (project in file("lobby-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `lobby-impl` = (project in file("lobby-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .settings(Seq(
    Compile / doc / sources := Nil,
    Compile / packageDoc / publishArtifact := false,
    publishArtifact in makePom := false
  )) //setting to turn off not needed actions to speed up artifact packaging
  .dependsOn(`lobby-api`)