object BuildSettings {
  import sbt._
  import Keys._

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-unchecked", "-Ywarn-dead-code"),
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),
    javaOptions += "-Djava.util.logging.config.file=logging.properties",
    javaOptions += "-Xmx2G",
    //scalaVersion := "2.11.0-M5",
    outputStrategy := Some(StdoutOutput),
    // fork := true,
    maxErrors := 1,
    // addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full),
    resolvers ++= Seq(
      Resolver.mavenLocal,
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases"),
      Resolver.typesafeRepo("snapshots"),
      Resolver.sonatypeRepo("snapshots")
    ),
    parallelExecution in Test := false
  )

}
