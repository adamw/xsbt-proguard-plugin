seq(ScriptedPlugin.scriptedSettings :_*)

scriptedBufferLog := false

sbtPlugin := true

name := "xsbt-proguard-plugin"

organization := "com.github.siasia"

version <<= sbtVersion(v => v)

resolvers += "Siasia github repo" at "http://siasia.github.com/maven2"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/ivy-releases/"

libraryDependencies += "net.sf.proguard" % "proguard" % "4.6"

scalacOptions += "-deprecation"

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "siasia.github.com" / "maven2")(Patterns(true, Resolver.mavenStyleBasePattern)))
