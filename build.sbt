import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

ThisBuild / organization := "dev.rocky"
ThisBuild / useCoursier := false
ThisBuild / homepage := Some(url("https://github.com/therockstorm/sbt-bootstrap"))

lazy val `sbt-bootstrap` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(Seq(
    addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.11"),
    addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.1"),
    addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1"),
    addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.0"),
    credentials += Credentials(
      "GnuPG Key ID",
      "gpg",
      "70DE2AE449C3C2F76EAF6F08B9376DD8AC89A652",
      "ignored" // passwords supplied by pinentry
    ),
    name := "sbt-bootstrap",
    publishMavenStyle := true,
    publishTo := sonatypePublishToBundle.value,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      publishArtifacts,
      setNextVersion
    ),
    sbtPlugin := true
  ): _*)
