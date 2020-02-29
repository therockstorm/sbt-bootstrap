package dev.rocky.sbt

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys._
import sbt._
import scalafix.sbt.ScalafixPlugin.autoImport.{scalafix, scalafixConfigSettings}

object BootstrapIt extends AutoPlugin {
  override def projectSettings: Seq[Setting[_]] =
    Defaults.itSettings ++
      inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings) ++
      inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest)) :+
      ((test in Test) := (test in Test).dependsOn((scalafix in IntegrationTest).toTask("")).value)
}
