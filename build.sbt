name := "freq-count"

organization := "hu.sztaki"

version := "2.0"

scalaVersion := "2.12.8"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "org.mockito" % "mockito-core" % "2.0.28-beta"

updateOptions := updateOptions.value.withGigahorse(false)

publishConfiguration := publishConfiguration.value.withOverwrite(true)

resolvers ++= Seq(
  "Soft-props repository" at "http://dl.bintray.com/content/softprops/maven",
  "Lightshed Maven repository" at "http://dl.bintray.com/content/lightshed/maven",
  "Seasar" at "https://www.seasar.org/maven/maven2/"
)