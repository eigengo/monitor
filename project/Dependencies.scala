object Dependencies {
  import sbt._
  import Keys._

  // to help resolve transitive problems, type:
  //   `sbt dependency-graph`
  //   `sbt test:dependency-tree`
  val bad = Seq(
    ExclusionRule(name = "log4j"),
    ExclusionRule(name = "commons-logging"),
    ExclusionRule(name = "commons-collections"),
    ExclusionRule(organization = "org.slf4j")
  )

  val aspectj_version = "1.7.3"

  val aspectj_weaver = "org.aspectj"  % "aspectjweaver" % aspectj_version

  object akka {
    val akka_version = "2.2.1"

    val actor   = "com.typesafe.akka" %% "akka-actor"   % akka_version
    val testkit = "com.typesafe.akka" %% "akka-testkit" % akka_version
  }

  object spray {
    val spray_version = "1.2-RC2"

    val can   = "io.spray" % "spray-can"   % spray_version
    val http  = "io.spray" % "spray-http"  % spray_version
    val httpx = "io.spray" % "spray-httpx" % spray_version
  }

  val typesafe_config  = "com.typesafe"	      % "config"                % "1.0.2"
  val dogstatsd_client = "com.indeed"         % "java-dogstatsd-client" % "2.0.7"

  val specs2           = "org.specs2"        %% "specs2"                % "2.2.3"
}
