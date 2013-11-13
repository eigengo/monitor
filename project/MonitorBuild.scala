import sbt._
import Keys._

object MonitorBuild extends Build {

  override val settings = super.settings ++ Seq(
    organization := "org.eigengo.monitor",
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.10.2"
  )

  def module(dir: String, extraSettings: Seq[Setting[_]] = Nil) = Project(id = dir, base = file(dir), 
    settings = BuildSettings.buildSettings ++ extraSettings)
  
  import Dependencies._
  import sbtunidoc.Plugin.UnidocKeys._
  import sbtunidoc.Plugin._
  import com.typesafe.sbt.SbtSite.site
  import com.typesafe.sbt.site.SphinxSupport
  import com.typesafe.sbt.site.SphinxSupport.{ enableOutput, generatePdf, generatedPdf, generateEpub, generatedEpub, sphinxInputs, sphinxPackages, Sphinx }
  import com.typesafe.sbt.preprocess.Preprocess.{ preprocess, preprocessExts, preprocessVars, simplePreprocess }

  lazy val root = Project(
    id = "parent", 
    base = file("."), 
    settings = BuildSettings.buildSettings ++ SphinxSupport.settings ++ scalaJavaUnidocSettings ++ unidocSettings ++ Seq(
      unidocConfigurationFilter in (TestScalaUnidoc, unidoc) := inConfigurations(Compile, Test),
      unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject,
      // generate online version of docs
      sphinxInputs in Sphinx <<= sphinxInputs in Sphinx in LocalProject(docs.id) map { inputs => inputs.copy(tags = inputs.tags :+ "online") },
      // don't regenerate the pdf, just reuse the akka-docs version
      generatedPdf in Sphinx <<= generatedPdf in Sphinx in LocalProject(docs.id) map identity,
      generatedEpub in Sphinx <<= generatedEpub in Sphinx in LocalProject(docs.id) map identity,
      // run options
      javaOptions in run += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.3.jar",
      fork in run := true,
      connectInput in run := true,
      mainClass in (Compile, run) := Some("org.eigengo.monitor.example.akka.Main")),
    aggregate = Seq(agent, output, output_statsd, agent_akka, agent_spray, agent_play, example_akka, docs)) dependsOn (example_akka)

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
  lazy val output = module("output") settings (
    libraryDependencies += typesafe_config,
    libraryDependencies += specs2 % "test"
  )
  lazy val output_statsd = module("output-statsd") dependsOn (output) settings (
  	libraryDependencies += dogstatsd_client,
    libraryDependencies += akka.actor,
    libraryDependencies += specs2 % "test"
  )
  lazy val test = module("test") dependsOn (output) settings (
  	libraryDependencies += specs2,
    libraryDependencies += akka.testkit
  )
  lazy val agent_akka = module("agent-akka", BuildSettings.aspectjCompileSettings) dependsOn (agent, output, test % "test") settings (
  	libraryDependencies += aspectj_weaver,
  	libraryDependencies += akka.actor,

    javaOptions in Test += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.3.jar",
    fork in Test := true
  )
  lazy val agent_spray = module("agent-spray") dependsOn(agent, output)
  lazy val agent_play  = module("agent-play")  dependsOn(agent, output)

  lazy val example_akka = module("example-akka") dependsOn(agent_akka, output_statsd) settings (
    libraryDependencies += akka.actor
  )
  lazy val example_spray = module("example-spray") dependsOn(agent_spray, output_statsd) settings (
    libraryDependencies += spray.can,
    libraryDependencies += spray.httpx
  )

  lazy val docs = Project(
    id = "docs",
    base = file("docs"),
    dependencies = Seq(agent, output, agent_akka, agent_spray, agent_play, example_akka),
    settings = BuildSettings.buildSettings ++ site.settings ++ site.sphinxSupport() ++ site.publishSite ++ sphinxPreprocessing ++ Seq(
      sourceDirectory in Sphinx <<= baseDirectory / "rst",
      sphinxPackages in Sphinx <+= baseDirectory { _ / "_sphinx" / "pygments" },
      enableOutput in generatePdf in Sphinx := true,
      enableOutput in generateEpub in Sphinx := true,
      unmanagedSourceDirectories in Test <<= sourceDirectory in Sphinx apply { _ ** "code" get },
      // libraryDependencies ++= Dependencies.docs,
      publishArtifact in Compile := false
      //unmanagedSourceDirectories in ScalariformKeys.format in Test <<= unmanagedSourceDirectories in Test,
      //testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a"),
      //reportBinaryIssues := () // disable bin comp check
    )
  )

  // preprocessing settings for sphinx
  lazy val sphinxPreprocessing = inConfig(Sphinx)(Seq(
    target in preprocess <<= baseDirectory / "rst_preprocessed",
    preprocessExts := Set("rst", "py"),
    // customization of sphinx @<key>@ replacements, add to all sphinx-using projects
    // add additional replacements here
    preprocessVars <<= (scalaVersion, version) { (s, v) =>
      val isSnapshot = v.endsWith("SNAPSHOT")
      val BinVer = """(\d+\.\d+)\.\d+""".r
      Map(
        "version" -> v,
        "scalaVersion" -> s,
        "crossString" -> (s match {
            case BinVer(_) => ""
            case _         => "cross CrossVersion.full"
          }),
        "jarName" -> (s match {
            case BinVer(bv) => "akka-actor_" + bv + "-" + v + ".jar"
            case _          => "akka-actor_" + s + "-" + v + ".jar"
          }),
        "binVersion" -> (s match {
            case BinVer(bv) => bv
            case _          => s
          }),
        //"sigarVersion" -> Dependencies.Compile.sigar.revision,
        "github" -> "http://github.com/akka/akka/tree/%s".format((if (isSnapshot) "master" else "v" + v))
      )
    },
    preprocess <<= (sourceDirectory, target in preprocess, cacheDirectory, preprocessExts, preprocessVars, streams) map {
      (src, target, cacheDir, exts, vars, s) => simplePreprocess(src, target, cacheDir / "sphinx" / "preprocessed", exts, vars, s.log)
    },
    sphinxInputs <<= (sphinxInputs, preprocess) map { (inputs, preprocessed) => inputs.copy(src = preprocessed) }
  )) ++ Seq(
    cleanFiles <+= target in preprocess in Sphinx
  )


}
