import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.ruimorais"
ThisBuild / organizationName := "Rui Morais"

lazy val root = (project in file("."))
  .settings(
    name := "blog"
  )

  scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

  lazy val refinedVersion = "0.9.4"

  lazy val catsVersion = "1.6.0"
  
  lazy val catsEffectVersion = "1.2.0"
  
  libraryDependencies ++= Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.10.2",
    "eu.timepit" %% "refined" % refinedVersion,
    "eu.timepit" %% "refined-cats" % refinedVersion,
    "eu.timepit" %% "refined-pureconfig" % refinedVersion,
    "eu.timepit" %% "refined-scalacheck" % refinedVersion,
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-laws" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "cats-effect-laws" % catsEffectVersion,
    "org.scalatest" %% "scalatest" % "3.0.5",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

/* Hugo */
enablePlugins(HugoPlugin)
sourceDirectory in Hugo := baseDirectory.value / "hugo"
baseURL in Hugo := uri("https://blog.ruimorais.com")

/* GitHubPages */
enablePlugins(GhpagesPlugin)
scmInfo := Some(ScmInfo(url("https://github.com/rmorais/blog"), "git@github.com:rmorais/blog.git"))
git.remoteRepo := scmInfo.value.get.connection
ghpagesNoJekyll := true

/* Mdoc */
enablePlugins(MdocPlugin)
mdocIn := baseDirectory.value / "posts"
mdocOut := (sourceDirectory in Hugo).value / "content" / "post"