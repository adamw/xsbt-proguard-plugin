resolvers += Resolver.url("Typesafe Repo", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns)

libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)
