import sbt._
import Keys._
import sbtrelease._

/**
 * This object includes the publishing mechanism. We publish to [Sonatype](https://oss.sonatype.org/).
 * The code follows the [Publishing](http://www.scala-sbt.org/release/docs/Detailed-Topics/Publishing)
 * guide, but uses the full-blown ``.scala`` syntax instead of the ``.sbt`` syntax.
 */
object Publish {

  /**
   * This sequence of SBT settings is the equivalent of writing
   * {{{
   * pomExtra  := ...
   * publishTo := ...
   * }}}
   * and others in the ``.sbt`` syntax.
   *
   * To keep things readable, we have pulled out the actual values like ``akkaExtrasPomExtra``, which
   * is a proper variable in the ``Publish`` object; and we refer to it when we construct the SBT
   * settings in this sequence. (Viz ``pomExtra := akkaExtrasPomExtra``.)
   */
  lazy val settings = Seq(
    crossPaths := false,
    pomExtra := pomExtraXml,
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      // versions that end with ``SNAPSHOT`` go to the Snapshots repository on Sonatype;
      // anything else goes to releases on Sonatype.
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    credentials ++= sonatypeCredentials,
    organizationName := "Eigengo",
    organizationHomepage := Some(url("http://www.eigengo.com")),
    publishMavenStyle := true,
    // Maven central cannot allow other repos.  
    // TODO - Make sure all artifacts are on central.
    pomIncludeRepository := { x => false }
  )

  /**
   * We construct _proper_ Maven-esque POMs to be able to release on Maven.
   */
  val pomExtraXml = (
    <url>https://github.com/eigengo/monitor</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:eigengo/monitor.git</url>
      <connection>scm:git@github.com:eigengo/monitor.git</connection>
      <developerConnection>scm:git@github.com:eigengo/monitor.git</developerConnection>
      <tag>HEAD</tag>
    </scm>
    <developers>
      <developer>
        <id>jan.machacek</id>
        <name>Jan Machacek</name>
        <email>jan.machacek@gmail.com</email>
      </developer>
      <developer>
        <id>alexl</id>
        <name>Alex Lashford</name>
        <email>alexl@cakesolutions.net</email>
      </developer>
      <developer>
        <id>anirvanc</id>
        <name>Anirvan Chakraborty</name>
        <email>anirvanc@cakesolutions.net</email>
      </developer>
      <developer>
        <id>hughsimpson</id>
        <name>Hugh Simpson</name>
        <email>hughs@cakesolutions.net</email>
      </developer>
    </developers>
  )

  /**
   * We load the Sonatype credentials from the ``~/.sonatype`` file. This file must contain
   * four lines in this format:
   * {{{
   * realm=Sonatype Nexus Repository Manager
   * host=oss.sonatype.org
   * user=<Your-Username>
   * password=<Your-Password>
   * }}}
   */  
  val sonatypeCredentials = Seq(Credentials(Path.userHome / ".sonatype"))

}
