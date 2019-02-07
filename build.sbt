import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.ruimorais"
ThisBuild / organizationName := "Rui Morais"

lazy val root = (project in file("."))
  .settings(
    name := "blog"
  )
  
/* Hugo */
enablePlugins(HugoPlugin)
sourceDirectory in Hugo := baseDirectory.value / "hugo"
baseURL in Hugo := uri("https://blog.ruimorais.com")

/* GitHubPages */
enablePlugins(GhpagesPlugin)
git.remoteRepo := "git@github.com:rmorais/blog.git"