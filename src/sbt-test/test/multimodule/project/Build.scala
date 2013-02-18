import sbt._
import Keys._
import ProguardPlugin._

object MultimoduleBuild extends Build {
  val buildSettings = Defaults.defaultSettings ++ ProguardPlugin.proguardSettings ++ Seq(
    scalaVersion  := "2.9.1",
    proguardOptions += keepMain("MainmoduleTest")
  )

  lazy val submodule1: Project = Project(
    "submodule1",
    file("submodule1"),
    settings = buildSettings
  )

  lazy val submodule2: Project = Project(
    "submodule2",
    file("submodule2"),
    settings = buildSettings
  )

  lazy val mainmodule: Project = Project(
    "mainmodule",
    file("mainmodule"),
    settings = buildSettings
  ) dependsOn(submodule1, submodule2)
}