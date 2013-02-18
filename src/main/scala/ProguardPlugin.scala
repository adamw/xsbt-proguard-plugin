import sbt._

import Project.Initialize
import Keys._

import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

import java.io.File
import java.util.Properties

object ProguardPlugin extends Plugin {
	def keepLimitedSerializability = """
	-keepclassmembers class * implements java.io.Serializable {
		static long serialVersionUID;
	private void writeObject(java.io.ObjectOutputStream);
	private void readObject(java.io.ObjectInputStream);
	java.lang.Object writeReplace();
	java.lang.Object readResolve();
	}
	"""

	def keepSerializability = "-keep class * implements java.io.Serializable { *; }"

	def keepAllScala = "-keep class scala.** { *; }"

	def keepMain (name :String) =
		"-keep public class " + name + " { static void main(java.lang.String[]); }"

	private def rtJarPath = {
		val javaHome = new java.io.File(System.getProperty("java.home"))
		val stdLocation = javaHome / "lib" / "rt.jar"
		val osXLocation = new java.io.File(javaHome.getParent()) / "Classes"/ "classes.jar"
		if (stdLocation.asFile.exists())
			stdLocation
		else if (osXLocation.asFile.exists())
			osXLocation
		else
			throw new IllegalStateException("Unknown location for rt.jar")
	}
	val proguard = TaskKey[Unit]("proguard")
	val minJarPath = SettingKey[File]("min-jar-path")

	private implicit def pathEscape(p: File) = new {
		def escaped: String = '"' + Path.absolute(p).toString.replaceAll("\\s", "\\ ") + '"'
	}

	val proguardDefaultArgs = SettingKey[Seq[String]]("proguard-default-args")
	val proguardOptions = SettingKey[Seq[String]]("proguard-options")
	val makeInJarFilter = SettingKey[String => String]("makeInJarFilter")

	private val proguardArgs = TaskKey[List[String]]("proguard-args")
	val proguardInJars = TaskKey[Seq[File]]("proguard-in-jars")
	val proguardInJarsTask = TaskKey[Seq[File]]("proguard-in-jars-task")
	val proguardLibraryJars = TaskKey[Seq[File]]("proguard-library-jars")

	def proguardInJarsTaskImpl: Initialize[Task[Seq[File]]] = {
		(dependencyClasspath in Compile, proguardInJars, proguardLibraryJars) map {
			(dc, pij, plj) =>
				import Build.data
			data(dc).filterNot(plj.contains) ++ pij
		}
	} 

	def proguardArgsTask: Initialize[Task[List[String]]] = {
		(proguardLibraryJars, proguardInJarsTask, artifactPath in (Compile, packageBin), makeInJarFilter, minJarPath, proguardDefaultArgs, proguardOptions, packageBin in Compile, streams) map {
			(plj, pij, jp, mijf, mjp, pda, po, pb, s) =>
				val proguardInJarsArg = {
					val inPaths = pij.foldLeft(Map.empty[String, File])((m, p) => m + (p.getName -> p)).values.iterator
					"-injars" :: (List(jp.escaped).iterator ++ inPaths.map(p => p.escaped+"("+mijf(p.asFile.getName)+")")).mkString(File.pathSeparator) :: Nil
				}
			val proguardOutJarsArg = "-outjars" :: mjp.escaped :: Nil
			val proguardLibJarsArg = {
				val libPaths = plj.foldLeft(Map.empty[String, File])((m, p) => m + (p.getName -> p)).values.iterator
				if (libPaths.hasNext) "-libraryjars" :: libPaths.map(_.escaped).mkString(File.pathSeparator) :: Nil else Nil
			}
			val args = proguardInJarsArg ::: proguardOutJarsArg ::: proguardLibJarsArg ::: pda.toList ::: po.toList
			s.log.debug("Proguard args: " + args)
			args
		}
	}

	def proguardTask(args: List[String], bd: File) {
		val config = new ProGuardConfiguration
    new ConfigurationParser(args.toArray[String], bd, new Properties()).parse(config)
		new ProGuard(config).execute
	}

	val proguardSettings = Seq(
		minJarPath <<= (crossTarget, projectID, artifact, scalaVersion, artifactName) { (t, module, a, sv, toString) => t / toString(ScalaVersion(sv, CrossVersion binaryScalaVersion sv), module.copy(revision = module.revision + ".min"), a) asFile },
		proguardOptions := Nil,
		makeInJarFilter := { (file) => "!META-INF/MANIFEST.MF" },
		proguardDefaultArgs := Seq("-dontwarn", "-dontoptimize", "-dontobfuscate"),
		proguardLibraryJars := { (rtJarPath :PathFinder).get },
		proguardInJars <<= (scalaInstance) map { (si) => Seq(si.libraryJar) },
		proguardInJarsTask <<= proguardInJarsTaskImpl,
		proguardArgs <<= proguardArgsTask,
		proguard <<= (packageBin in Compile, proguardArgs in Compile, baseDirectory) map { (_, args, bd) => proguardTask(args, bd) }
	)
}
