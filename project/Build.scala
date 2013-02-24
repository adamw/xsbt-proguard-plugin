import sbt._
import ScriptedPlugin._
import Keys._

object ProguardPlugin extends Build {
  def rootSettings: Seq[Setting[_]] = Seq(
    scriptedBufferLog := false,
    scriptedLaunchOpts += "-XX:MaxPermSize=128m",
    sbtPlugin := true,
    name := "xsbt-proguard-plugin",
    organization := "org.scala-sbt",
    version := "0.1.3",
    libraryDependencies += "net.sf.proguard" % "proguard-base" % "4.8",
    scalacOptions += "-deprecation",
    publishMavenStyle := false,
    publishTo <<= (version) { version: String =>
      val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
      val (name, url) = if (version.contains("-SNAPSHOT"))
        ("sbt-plugin-snapshots-scalasbt", scalasbt+"sbt-plugin-snapshots")
      else
        ("sbt-plugin-releases-scalasbt", scalasbt+"sbt-plugin-releases")
      Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
    },
    homepage := Some(new java.net.URL("https://github.com/adamw/xsbt-proguard-plugin")),
    licenses := ("GPLv2", new java.net.URL("http://www.gnu.org/licenses/gpl-2.0.html")) :: Nil,
    scmInfo := Some(ScmInfo(new java.net.URL("https://github.com/adamw/xsbt-proguard-plugin"),
      "scm:git:git@github.com:adamw/xsbt-proguard-plugin.git"))
  )
  lazy val root = Project("root", file(".")) settings(scriptedSettings ++ rootSettings :_*)
}
