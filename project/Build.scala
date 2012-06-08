import sbt._
import Keys._

object ProguardBuild extends Build {
	def rootSettings: Seq[Setting[_]] = Seq(
		sbtPlugin := true,
		projectID <<= (organization,moduleName,version,artifacts,crossPaths){ (org,module,version,as,crossEnabled) =>
			ModuleID(org, module, version).cross(crossEnabled).artifacts(as : _*)
		},
		name := "xsbt-proguard-plugin",
		organization := "com.github.aloiscochard",
		version <<= sbtVersion(_ + "-0.1.2"),
		libraryDependencies += "net.sf.proguard" % "proguard-base" % "4.7",
		scalacOptions += "-deprecation",
    scalaVersion := "2.9.2",
    crossScalaVersions := Seq("2.9.0", "2.9.0-1", "2.9.1", "2.9.1-1", "2.9.2")
  )

	lazy val root = Project("root", file(".")) settings(Publishing.settings ++ rootSettings :_*)
}

object Publishing extends Sonatype(ProguardBuild) {
  def projectUrl    = "https://github.com/aloiscochard/xsbt-proguard-plugin"
  def developerId   = "aloiscochard"
  def developerName = "Alois Cochard"
  def licenseName   = "Apache License 2"
  def licenseUrl    = "http://www.apache.org/licenses/LICENSE-2.0.html"
}

