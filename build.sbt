seq(ScriptedPlugin.scriptedSettings :_*)

scriptedBufferLog := false

sbtPlugin := true

name := "xsbt-proguard-plugin"

organization := "com.github.siasia"

version <<= appConfiguration(app => app.provider.id.version)

resolvers += "Siasia github repo" at "http://siasia.github.com/maven2"

libraryDependencies += "net.sf.proguard" % "proguard" % "4.6"

publishMavenStyle := true

publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "siasia.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))
