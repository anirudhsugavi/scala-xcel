import java.lang.management.ManagementFactory

ThisBuild / scalaVersion := "3.3.6"

ThisBuild / organization := "com.sugavi"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalafmtOnCompile := true

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val model = project
  .in(file("model"))
  .settings(scalafixSettings)
  .settings(name := "model")

lazy val xcel = project
  .in(file("xcel"))
  .settings(scalafixSettings)
  .settings(
    name := "xcel",
    libraryDependencies ++= Seq(
      "org.apache.poi"     % "poi"             % "5.4.1",
      "org.apache.poi"     % "poi-ooxml"       % "5.4.1",
      "org.scalatest"     %% "scalatest"       % "3.2.19"   % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % Test
    ),
  )
  .dependsOn(model)

lazy val `scala-excel` = project
  .in(file("."))
  .aggregate(model, xcel)
  .settings(
    name           := "scala-xcel",
    publish / skip := true,
    Test / fork    := !isDebug,
  )

lazy val scalafixSettings = Seq(
  semanticdbEnabled := true,
  scalacOptions ++= Seq(
    "-Wall",
    "-feature",
    "-Wunused:unsafe-warn-patvars",
  )
)

enablePlugins(
  ScalafmtPlugin,
  ScalafixPlugin,
)

def isDebug: Boolean = ManagementFactory.getRuntimeMXBean.getInputArguments.contains("-Xdebug")
