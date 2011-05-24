##Usage

Requires [XSBT](http://github.com/harrah/xsbt/tree/0.9).

Maven artifacts are created for 0.9.4, 0.9.6, 0.9.7 and 0.9.8 versions.

###Adding the plugin to your build

To use the plugin in a project, you need to create `project/plugins/build.sbt`:

    resolvers += "Proguard plugin repo" at "http://siasia.github.com/maven2"

    libraryDependencies <<= (libraryDependencies, appConfiguration) { 
      (deps, app) =>
      deps :+ "com.github.siasia" %% "xsbt-proguard-plugin" % app.provider.id.version
    }
		
This adds plugin version corresponding to your sbt version into your build.

In case if you have a xsbt version different from one used in Maven artifacts or want to use development version of the plugin, you need to create a `project/plugins/project/Build.scala`:    

    import sbt._
    object PluginDef extends Build {
      lazy val projects = Seq(root)
      lazy val root = Project("plugins", file(".")) dependsOn(proguard)
      lazy val proguard = uri("git://github.com/siasia/xsbt-proguard-plugin.git")
    }
    
###Injecting the Plugin into desired project

Inject the proguard settings into your project through `build.sbt`:

    seq(ProguardPlugin.proguardSettings :_*)
    
Add proguard keep options in `build.sbt`. Main class keep example:

    proguardOptions += keepMain("Test")

This will add a `proguard` action which will run Proguard and generate output in `target/<scala-version>/<project-name-version>.min.jar`. You may consult `min-jar-path` setting to see the actual path:

    > min-jar-path
    [info] /home/siasia/projects/xsbt-proguard-test/target/scala-2.8.1.final/root_2.8.1-0.1.min.jar
		
##Examples

If you want to add some custom jar into the set of input jar files. Do so as follows:

    proguardInJar += (Path.userHome / "lib" / "webspec" / "runtime.jar").asFile
    
Scala Library is already there.

If you wish to include all Scala classes in your output (regardless of whether
they are used), use the following option:

    proguardOptions ++= Seq(
      ...,
      keepAllScala
    )

If you wish to keep the `main()` entry point of a class, use:

    proguardOptions ++= Seq(
      ...,
      keepMain("somepackage.SomeClass")
    )

If you wish to keep everything that is `Serializable`, use:

    proguardOptions ++= Seq(
      ...,
      keepLimitedSerializability
    )

By default Proguard will be instructed to include everything except classes
from the Java runtime. To treat additional libraries as external (i.e. to
add them to the list of `-libraryjars` passed to Proguard), do the following. Here comes the example how to select a module named "httpclient" from the library dependencies:
    
    proguardLibraryJars <<= (proguardLibraryJars, update) map {
	    (libJars, report) =>
	    val httpclientJars = report.select(module = moduleFilter(name = "httpclient"))
      libJars ++ httpclientJars
    }

By default all jar files passed to Proguard (except for the one that contains
your project's classes) are filtered using
`somejar.jar(!META-INF/MANIFEST.MF)`. This is necessary to prevent conflicts
when Proguard generates a single final jar. If you wish to filter other
resources from a jar file, do the following:

    makeInJarFilter <<= (makeInJarFilter) {
      (makeInJarFilter) => {
        (file) => file match {
          case "httpcore-4.1.jar" => makeInJarFilter(file) + ",!META-INF/**"
          case _ => makeInJarFilter(file)
        }
      }
    }

The argument to `makeJarFilter` will be the filename of the jar file in
question (minus any path). Note that your project's jar file is always included
without any filtering.

Other customizations are possible, take a look at the source to [ProguardPlugin](http://github.com/siasia/xsbt-proguard-plugin/blob/master/src/main/scala/ProguardPlugin.scala).

##Hacking on the plugin

If you need make modifications to the plugin itself, you can compile and install it locally (you need at least xsbt 0.9.x to build it):

    $ git clone git://github.com/siasia/xsbt-proguard-plugin.git
    $ cd xsbt-proguard-plugin
    $ xsbt publish-local

##License

This plugin depends upon ProGuard (http://proguard.sourceforge.net/),
which is licensed under the GNU General Public License version 2.0.
As such, this plugin is distributed under the same license; you are
free to use and modify this work so long as any derivative work complies
with the distribution terms. See LICENSE for additional information.

##Credits

This is a port of [sbt-proguard-plugin](http://github.com/nuttycom/sbt-proguard-plugin) by Kris Nuttycombe.
