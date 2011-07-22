resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies <+= sbtVersion("org.scala-tools.sbt" %% "scripted-plugin" % _)
