##Usage

Requires [XSBT](http://github.com/harrah/xsbt).

Maven artifacts are created for 0.11.0 version.

###Adding the plugin to your build

####1. Up to SBT 0.11.1

To use the plugin in a project, you need to create `project/plugins/build.sbt`(**since sonatype doesn't allow non standard maven layout we can't use `addSbtPlugin` here**):

    libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))
		
This adds plugin version corresponding to your sbt version into your build.

In case if you have a xsbt version different from one used in Maven artifacts or want to use development version of the plugin, you need to create a `project/plugins/project/Build.scala`:    

    import sbt._
    object PluginDef extends Build {
      override def projects = Seq(root)
      lazy val root = Project("plugins", file(".")) dependsOn(proguard)
      lazy val proguard = uri("git://github.com/siasia/xsbt-proguard-plugin.git")
    }
    
####2. For SBT 0.11.2 onwards

Follow the instructions above, but note that the `project/plugins` folder has been deprecated in SBT 0.11.2.

Therefore your plugin .sbt file should be created as `project/plugins.sbt`, or alternatively your Build.scala created as `project/project/Build.scala`. Make sure to remove your `project/plugins/` directory if you are upgrading.
    
###Injecting the Plugin into desired project

####1. Using build.sbt

To inject the proguard settings into your project through `build.sbt`:

    seq(ProguardPlugin.proguardSettings :_*)
    
Add proguard keep options in `build.sbt`. Main class keep example:

    proguardOptions += keepMain("Test")

####2. In your Build.scala

Alternatively, you can configure the proguard settings within your project's `Build.scala` or equivalent:

    import ProguardPlugin._
    lazy val proguard = proguardSettings ++ Seq(
      proguardOptions := Seq(keepMain("Test"))
    )
    
And then include `proguard` in your `Project` definition as usual:

    lazy val myProject = Project("my-project", file("."))
      .settings(proguard: _*)
      ...

####Test

Either of these will add a `proguard` action which will run Proguard and generate output in `target/<scala-version>/<project-name-version>.min.jar`. You may consult `min-jar-path` setting to see the actual path:

    > min-jar-path
    [info] /home/siasia/projects/xsbt-proguard-test/target/scala-2.8.1.final/root_2.8.1-0.1.min.jar
		
##Examples

If you want to add some custom jar into the set of input jar files. Do so as follows:

    proguardInJars += Path.userHome / "lib" / "webspec" / "runtime.jar"
    
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
    
    proguardLibraryJars <++= (update) map (_.select(module = moduleFilter(name = "httpclient")))

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
