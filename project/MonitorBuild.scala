import sbt._
import Keys._

object MonitorBuild extends Build {
  import BuildSettings._

  override val settings = super.settings ++ Seq(
    organization := "org.eigengo",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.2"
  )

  def module(dir: String, extraSettings: Seq[Setting[_]] = Nil) = Project(id = dir, base = file(dir), 
    settings = BuildSettings.buildSettings ++ extraSettings)
  import Dependencies._

  lazy val root = Project(
    id = "parent", 
    base = file("."), 
    settings = BuildSettings.buildSettings ++ Seq(
      mainClass in (Compile, run) := Some("org.eigengo.monitor.example.akka.Main")),
    aggregate = Seq(agent, output, agent_akka, agent_spray, agent_play, example_akka))

/*
  lazy val macros = module("macros") settings(
    libraryDependencies <++=
      (scalaVersion)(v => Seq(("org.scala-lang" % "scala-compiler" % v), ("org.scala-lang" % "scala-reflect" % v))))
  )
  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(libraryDependencies <++=
      (scalaVersion)(v => Seq(("org.scala-lang" % "scala-compiler" % v), ("org.scala-lang" % "scala-reflect" % v)))))
*/


  lazy val agent = module("agent") settings (
  	libraryDependencies += typesafe_config
  )
  lazy val output = module("output") 
  lazy val output_statsd = module("output-statsd") dependsOn (output) settings (
  	libraryDependencies += dogstatsd_client
  )
  lazy val test = module("test") dependsOn (output) settings (
  	libraryDependencies += specs2 % "test"
  )
  lazy val agent_akka = module("agent-akka", BuildSettings.aspectjCompileSettings) dependsOn (agent, output, test % "test") settings (
  	libraryDependencies += aspectj_weaver,
  	libraryDependencies += akka_actor,
  	libraryDependencies += specs2 % "test",
  	libraryDependencies += akka_testkit % "test",
  	libraryDependencies += junit % "test",

    javaOptions in Test += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.3.jar",
    fork in Test := true
  )
  lazy val agent_spray = module("agent-spray") dependsOn(agent, output)
  lazy val agent_play  = module("agent-play")  dependsOn(agent, output)

  lazy val example_akka = module("example-akka") dependsOn(agent_akka, output_statsd) settings (
  	libraryDependencies += akka_actor,

    javaOptions in run += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.3.jar",
    fork in run := true,
    connectInput in run := true
  )

}
