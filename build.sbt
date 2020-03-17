name := "freq-count"

organization := "hu.sztaki"

version := "2.0"

scalaVersion := "2.12.11"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % Test

libraryDependencies += "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test

updateOptions := updateOptions.value.withGigahorse(false)

publishConfiguration := publishConfiguration.value.withOverwrite(true)

resolvers ++= Seq("Maven Central" at "https://repo1.maven.org/maven2/")
