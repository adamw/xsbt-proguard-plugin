import sbt._

import Project.Initialize
import Keys._

import proguard.{Configuration=>ProGuardConfiguration, ProGuard, ConfigurationParser}

import java.io.File

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
  val proguard = TaskKey[Unit]("proguard")
  val minJarName = SettingKey[ArtifactName]("min-jar-name")
  val minJarPath = SettingKey[File]("min-jar-path")

  private implicit def pathEscape(p: Path) = new {
    def escaped: String = '"' + p.absolutePath.replaceAll("\\s", "\\ ") + '"'
  }

  val proguardDefaultArgs = SettingKey[Seq[String]]("proguard-default-args")
  val proguardOptions = SettingKey[Seq[String]]("proguard-options")
  val makeInJarFilter = SettingKey[String => String]("makeInJarFilter")

  private val proguardArgs = TaskKey[List[String]]("proguard-args")
  val proguardInJars = SettingKey[Seq[File]]("proguard-in-jars")
  val proguardInJarsTask = TaskKey[Seq[File]]("proguard-in-jars-task")
  val proguardLibraryJars = TaskKey[Seq[File]]("proguard-library-jars")

  def proguardInJarsTaskImpl: Initialize[Task[Seq[File]]] = {
    (fullClasspath in Compile, products in Compile, proguardInJars, proguardLibraryJars) map {
      (fc, ps, pij, plj) =>
      import Build.data
      data(fc).filterNot(data(ps).contains).filterNot(plj.contains) ++ pij
    }
  } 

  def proguardArgsTask: Initialize[Task[List[String]]] = {
    (proguardLibraryJars, proguardInJarsTask, jarPath in (Compile, packageBin), makeInJarFilter, minJarPath, proguardDefaultArgs, proguardOptions, packageBin in Compile, streams) map {
      (plj, pij, jp, mijf, mjp, pda, po, pb, s) =>
      val proguardInJarsArg = {
        val inPaths = pij.foldLeft(Map.empty[String, Path])((m, p) => m + (p.getName -> Path.fromFile(p))).values.iterator
        "-injars" :: (List(Path.fromFile(jp).escaped).iterator ++ inPaths.map(p => p.escaped+"("+mijf(p.asFile.getName)+")")).mkString(File.pathSeparator) :: Nil
      }
      val proguardOutJarsArg = "-outjars" :: Path.fromFile(mjp).escaped :: Nil
      val proguardLibJarsArg = {
        val libPaths = plj.foldLeft(Map.empty[String, Path])((m, p) => m + (p.getName -> Path.fromFile(p))).values.iterator
        if (libPaths.hasNext) "-libraryjars" :: libPaths.map(_.escaped).mkString(File.pathSeparator) :: Nil else Nil
      }
      val args = proguardInJarsArg ::: proguardOutJarsArg ::: proguardLibJarsArg ::: pda.toList ::: po.toList
      s.log.debug("Proguard args: " + args)
      args
    }
  }

  def proguardTask: Initialize[Task[Unit]] = {
    (proguardArgs in Compile, baseDirectory) map {
      (args, bd) =>
      val config = new ProGuardConfiguration
      new ConfigurationParser(args.toArray[String], bd).parse(config)
      new ProGuard(config).execute
    }
  }  

  val proguardSettings = Seq(
    minJarName <<= (jarName) { (n) => n.copy(version = n.version + ".min") },
    minJarPath <<= (target, minJarName, nameToString) { (t, n, toString) => t / toString(n) },
    proguardOptions := Nil,
    makeInJarFilter := { (file) => "!META-INF/MANIFEST.MF" },
    proguardDefaultArgs := Seq("-dontwarn", "-dontoptimize", "-dontobfuscate"),
    proguardLibraryJars := { (rtJarPath :PathFinder).getFiles },
    proguardInJars <<= (scalaInstance) { (si) => Seq(si.libraryJar) },
    proguardInJarsTask <<= proguardInJarsTaskImpl,
    proguardArgs <<= proguardArgsTask,
    proguard <<= proguardTask
  )
}
