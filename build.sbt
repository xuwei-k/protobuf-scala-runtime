import ReleaseTransformations._

scalaVersion in ThisBuild := "2.11.7"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.7", "2.12.0-M2")

organization in ThisBuild := "com.trueaccord.scalapb"

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target:jvm-1.7")
    case _ => Nil
  }
}

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  pushChanges
)

lazy val root = project.in(file(".")).
  aggregate(runtimeJS, runtimeJVM).
  settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

lazy val protobufRuntimeScala = crossProject.crossType(CrossType.Pure).in(file("."))
  .settings(
    name := "protobuf-runtime-scala",
    TaskKey[Unit]("compatibilityTest") := {
      import java.util.jar.JarFile
      import scala.collection.JavaConverters._
      import java.io.File
      val clazz = ".class"
      val jar = (packageBin in Compile).value
      println(jar)
      val classes = new JarFile(jar).entries.asScala.filter{ s =>
        ( s.isDirectory == false ) && {
          val n = s.toString
          n.endsWith(clazz) &&
          n.dropRight(clazz.length + 1).contains("$") == false
        }
      }.map{
        s => s.toString.dropRight(clazz.length).replace('/','.')
      }.toList
    }
  )
  .jvmSettings(
    // Add JVM-specific settings here
  )
  .jsSettings(
    // Add JS-specific settings here
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/trueaccord/protobuf-scala-runtime/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )
  
lazy val runtimeJS = protobufRuntimeScala.js
lazy val runtimeJVM = protobufRuntimeScala.jvm
