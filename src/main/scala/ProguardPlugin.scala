import sbt._

import Project.Initialize
import Keys._

import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

import java.io.File

object ProguardProject {
  val Description = "Aggregate and minimize the project's files and all dependencies into a single jar."
  def rtJarPath = {
    val javaHome = System.getProperty("java.home")
    val stdLocation = Path.fromFile(javaHome) / "lib" / "rt.jar"
    val osXLocation = Path.fromFile(new java.io.File(javaHome).getParent()) / "Classes"/ "classes.jar"
    if (stdLocation.asFile.exists())
      stdLocation
    else if (osXLocation.asFile.exists())
      osXLocation
    else
      throw new IllegalStateException("Unknown location for rt.jar")
  }
}

object ProguardPlugin extends Plugin {
  val proguard = TaskKey[Unit]("proguard")
  val minJarName = SettingKey[ArtifactName]("min-jar-name")
  val minJarPath = SettingKey[File]("min-jar-path")

  private implicit def pathEscape(p: Path) = new {
    def escaped: String = '"' + p.absolutePath.replaceAll("\\s", "\\ ") + '"'
  }

  val proguardDefaultArgs = SettingKey[List[String]]("proguard-default-args")
  val proguardOptions = SettingKey[List[String]]("proguard-options")

  private def makeInJarFilter (file :String) = "!META-INF/MANIFEST.MF"

  val proguardArgs = TaskKey[List[String]]("proguard-args")

  private def proguardArgsTask: Initialize[Task[List[String]]] = {
    (scalaInstance, fullClasspath in Compile, products in Compile, jarPath in (Compile, packageBin), minJarPath, proguardDefaultArgs, proguardOptions, packageBin in Compile, streams) map {
      (si, fc, ps, jp, mjp, pda, po, pb, s) =>
      val scalaLibraryPath = Path.fromFile(si.libraryJar)
      val proguardInJars = fc.filterNot(ps.contains)
      val proguardLibraryJars = (ProguardProject.rtJarPath :PathFinder)
      val proguardInJarsArg = {
        val inPaths = proguardInJars.foldLeft(Map.empty[String, Path])((m, p) => m + (p.data.getName -> Path.fromFile(p.data))).values.toIterator
        "-injars" :: (List(Path.fromFile(jp).escaped).elements ++ inPaths.map(p => p.escaped+"("+makeInJarFilter(p.asFile.getName)+")")).mkString(File.pathSeparator) :: Nil
      }
      val proguardOutJarsArg = "-outjars" :: Path.fromFile(mjp).escaped :: Nil
      val proguardLibJarsArg = {
        val libPaths = proguardLibraryJars.get.foldLeft(Map.empty[String, Path])((m, p) => m + (p.asFile.getName -> p)).values.toIterator
        if (libPaths.hasNext) "-libraryjars" :: libPaths.map(_.escaped).mkString(File.pathSeparator) :: Nil else Nil
      }
      val args = proguardInJarsArg ::: proguardOutJarsArg ::: proguardLibJarsArg ::: pda ::: po
      s.log.debug("Proguard args: " + args)
      args
    }
  }

  private def proguardTask: Initialize[Task[Unit]] = {
    (proguardArgs in Compile, baseDirectory) map {
      (args, bd) =>
      val config = new ProGuardConfiguration
      new ConfigurationParser(args.toArray[String], bd).parse(config)
      new ProGuard(config).execute
    }
  }  

  val newSettings = Seq(
    minJarName <<= (jarName) { (n) => n.copy(version = n.version + ".min") },
    minJarPath <<= (target, minJarName, nameToString) { (t, n, toString) => t / toString(n) },
    proguardOptions :== List(),
    proguardDefaultArgs :== List("-dontwarn", "-dontoptimize", "-dontobfuscate"),
    proguardArgs <<= proguardArgsTask,
		proguard <<= proguardTask
	)
}
