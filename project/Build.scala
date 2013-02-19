import sbt._
import ScriptedPlugin._
import Keys._

import scala.xml.{Elem, Node}

object ProguardPlugin extends Build {
	def rootSettings: Seq[Setting[_]] = Seq(
		scriptedBufferLog := false,
    scriptedLaunchOpts += "-XX:MaxPermSize=128m",
		sbtPlugin := true,
		projectID <<= (organization,moduleName,version,artifacts,crossPaths){ (org,module,version,as,crossEnabled) =>
			ModuleID(org, module, version).cross(crossEnabled).artifacts(as : _*)
		},
		name := "xsbt-proguard-plugin",
		organization := "org.scala-sbt",
		version <<= sbtVersion(_ + "-0.1.3-SNAPSHOT"),
		libraryDependencies += "net.sf.proguard" % "proguard-base" % "4.8",
		scalacOptions += "-deprecation",
		publishMavenStyle := false,
    publishTo <<= (version) { version: String =>
      val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
      val (name, url) = if (version.contains("-SNAPSHOT"))
        ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
      else
        ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
      Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
    },
		credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    homepage := Some(new java.net.URL("https://github.com/adamw/xsbt-proguard-plugin")),
    licenses := ("GPLv2", new java.net.URL("http://www.gnu.org/licenses/gpl-2.0.html")) :: Nil,
    scmInfo := Some(ScmInfo(new java.net.URL("https://github.com/adamw/xsbt-proguard-plugin"),
      "scm:git:git@github.com:adamw/xsbt-proguard-plugin.git"))
  )
	lazy val root = Project("root", file(".")) settings(scriptedSettings ++ rootSettings :_*)
}
