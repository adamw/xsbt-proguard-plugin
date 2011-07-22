resolvers += Resolver.url("Typesafe Repo", "http://repo.typesafe.com/typesafe/ivy-releases/")(Resolver.ivyStylePatterns)

libraryDependencies <+= sbtVersion("org.scala-tools.sbt" %% "scripted-plugin" % _)
