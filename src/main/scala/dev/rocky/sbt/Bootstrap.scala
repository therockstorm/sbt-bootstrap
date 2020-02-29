package dev.rocky.sbt

import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtCheckAll
import sbt.Keys._
import sbt._
import sbt.io.IO.write
import sbt.plugins.JvmPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.{scalafix, scalafixDependencies, scalafixSemanticdb}
import scoverage.ScoverageKeys.coverageHighlighting

import scala.sys.process._
import scala.util.Properties.envOrNone

object Bootstrap extends AutoPlugin {
  override def trigger: PluginTrigger =
    allRequirements

  override def requires: Plugins =
    JvmPlugin

  override def buildSettings: Seq[Setting[_]] =
    Seq(
      organization := "dev.rocky",
      scalafixDependencies ++= Seq(
        "com.github.vovapolu" %% "scaluzzi" % "0.1.3",
        "com.eed3si9n.fix" %% "scalafix-noinfer" % "0.1.0-M1"
      ),
      scalaVersion := "2.13.1",
      useCoursier := false,
      SettingKey[Unit]("lefthookInstall") := {
        val script = "./.lefthook.sh"
        write(
          file(script),
          """#!/bin/bash
            |
            |INSTALLER=brew
            |PACKAGE="Arkweid/lefthook/lefthook"
            |PREFIX="[sbt-bootstrap plugin]"
            |if [ "$(command -v $INSTALLER)" ]; then
            |  $INSTALLER ls --versions $PACKAGE > /dev/null || $INSTALLER install $PACKAGE
            |
            |  if [ -f "./lefthook.yml" ]; then
            |    lefthook install && echo "$PREFIX lefthook installed"
            |  else
            |    echo "$PREFIX lefthook.yml does not exist, skipping"
            |  fi
            |else
            |  echo "$PREFIX $INSTALLER not installed, skipping"
            |fi
            |"""
            .stripMargin.getBytes("UTF-8")
        )
        s"chmod +x $script".!
        s"$script".!
      },
      SettingKey[Unit]("scalafmtGenerateConfig") :=
        write(
          file(".scalafmt-common.conf"),
          """version = 2.3.2
            |
            |maxColumn: 100
            |rewrite {
            |  rules = [
            |    AvoidInfix,
            |    PreferCurlyFors,
            |    RedundantBraces,
            |    RedundantParens,
            |    SortImports,
            |    SortModifiers
            |  ]
            |  redundantBraces.maxLines = 1
            |}
            |
            |onTestFailure = "To fix, run `sbt scalafmtAll` from project's root directory"
            |"""
            .stripMargin.getBytes("UTF-8")
        ),
      SettingKey[Unit]("scalafixGenerateConfig") :=
        write(
          file(".scalafix-common.conf"),
          """# Built-in rules, https://scalacenter.github.io/scalafix/docs/rules/overview.html
            |# Config adapted from https://github.com/alejandrohdezma/sbt-fix-defaults/
            |rules = [
            |  Disable
            |  DisableSyntax
            |  LeakingImplicitClassVal
            |  MissingFinal
            |  NoInfer
            |  NoValInForComprehension
            |  ProcedureSyntax
            |  RemoveUnused
            |]
            |
            |Disable.symbols = [
            |  {
            |    symbol = "scala.util.Either.RightProjection.get"
            |    message = "RightProjection#get disabled, prefer RightProjection#toOption"
            |  }
            |  {
            |    symbol = "scala.util.Either.LeftProjection.get"
            |    message = "LeftProjection#get disabled, prefer LeftProjection#toOption"
            |  }
            |  {
            |    symbol = "scala.Option.get"
            |    message = "Option#get disabled, prefer Option#fold or Option#map"
            |  }
            |  {
            |    symbol = "scala.Some.get"
            |    message = "Option#get disabled, prefer Option#fold or Option#map"
            |  }
            |  {
            |    symbol = "scala.None.get"
            |    message = "Option#get disabled, prefer Option#fold or Option#map"
            |  }
            |  {
            |    symbol = "scala.util.Try.get"
            |    message = "Try#get disabled, prefer pattern matching"
            |  }
            |  {
            |    symbol = "scala.util.Success.get"
            |    message = "Success#get disabled, prefer pattern matching"
            |  }
            |  {
            |    symbol = "scala.util.Failure.get"
            |    message = "Failure#get disabled, prefer pattern matching"
            |  }
            |  {
            |    regex = "^\\Qscala/collection/mutable\\E.*$"
            |    message = "Mutable collections disabled, prefer Scala immutable or Java mutable collections for better performance"
            |  }
            |  {
            |    regex = "^\\Qscala/math/Big\\E.*$"
            |    message = "scala.math.Big* are broken (https://github.com/scala/bug/issues/9670), prefer java.math.BigDecimal"
            |  }
            |  {
            |    regex = {
            |      includes = [
            |        "^\\Qjava/io\\E.*$"
            |        "^\\Qscala/io/Source\\E.*$"
            |      ]
            |    }
            |    message = "Legacy blocking API, prefer java.nio"
            |  }
            |  {
            |    regex = "^\\Qjava/net/URL#\\E.*$"
            |    message = "URL uses network for equality, prefer URI"
            |  }
            |  {
            |    regex = {
            |      includes = [
            |        "^\\Qscala/util/Either.LeftProjection#get().\\E$"
            |        "^\\Qscala/util/Either.RightProjection#get().\\E$"
            |        "^\\Qscala/util/Try#get().\\E$"
            |        "^\\Qscala/Option#get().\\E$"
            |        "^\\Qscala/collection/IterableLike#head().\\E$"
            |      ]
            |    }
            |    message = "not a total function"
            |  }
            |]
            |
            |Disable.ifSynthetic = [
            |  "java/io/Serializable"
            |  "scala/Any"
            |  "scala/Product"
            |  "scala/Option.option2Iterable"
            |  "scala/Predef.any2stringadd"
            |  {
            |    regex = {
            |      includes = [
            |        "^\\Qscala/collection/MapLike#apply().\\E$"
            |        "^\\Qscala/collection/LinearSeqOptimized#apply().\\E$"
            |      ]
            |    }
            |    message = "not a total function"
            |  }
            |]
            |
            |DisableSyntax {
            |  noAsInstanceOf = true
            |  noContravariantTypes = true
            |  noCovariantTypes = true
            |  noDefaultArgs = true
            |  noFinalVal = true
            |  noFinalize = true
            |  noImplicitConversion = true
            |  noImplicitObject = true
            |  noIsInstanceOf = true
            |  noNulls = true
            |  noReturns = true
            |  noSemicolons = true
            |  noTabs = true
            |  noThrows = true
            |  noUniversalEquality = true
            |  noValInAbstract = true
            |  noValPatterns = true
            |  noVars = true
            |  noWhileLoops = true
            |  noXml = true
            |}
            |
            |NoInfer.symbols = [
            |  "scala.AnyVal"
            |  "scala.Any"
            |  "java.io.Serializable"
            |  "scala.Product."
            |  "scala.Predef.any2stringadd"
            |  "scala.Option.option2Iterable"
            |]
            |"""
            .stripMargin.getBytes("UTF-8")
        )
    )

  override def projectSettings: Seq[Setting[_]] = {
    val flags: Seq[String] = Seq(
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-unchecked",
      "-Wdead-code",
      "-Wextra-implicit",
      "-Xlint:adapted-args,nullary-unit,inaccessible,nullary-override,infer-any,missing-interpolator,doc-detached,private-shadow,type-parameter-shadow,poly-implicit-overload,option-implicit,delayedinit-select,package-object-classes,stars-align,constant,unused,nonlocal-return,implicit-not-found,serial,valpattern,eta-zero,eta-sam,deprecation",
      "-Yrangepos",
      "-Ywarn-unused"
    )
    Seq(
      addCompilerPlugin(scalafixSemanticdb),
      coverageHighlighting := true,
      scalacOptions ++= envOrNone("CI").map(_ => flags ++ Seq("-Werror")).getOrElse(flags),
      (test in Test) := (test in Test)
        .dependsOn(Def
          .sequential(
            (scalafix in Compile).toTask(""),
            (scalafix in Test).toTask(""),
            scalafmtCheckAll
          )
        )
        .value
    )
  }
}
