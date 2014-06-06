object BuildSettings {
  import sbt._
  import Keys._
  import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
  import org.scalastyle.sbt.ScalastylePlugin

  lazy val buildSettings = 
    Defaults.defaultSettings ++ 
    sbtunidoc.Plugin.unidocSettings ++ 
    sbtunidoc.Plugin.genjavadocExtraSettings ++ 
    Publish.settings ++ 
    aspectjCompileSettings ++
    ScalastylePlugin.Settings ++ 
    Seq(
      org.scalastyle.sbt.PluginKeys.config := file("project/scalastyle-config.xml"),
      scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7", "-deprecation", "-unchecked", "-Ywarn-dead-code"),
      scalacOptions in (Compile, doc) <++= (name in (Compile, doc), version in (Compile, doc)) map DefaultOptions.scaladoc,
      //javacOptions in Compile ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),
      //scalacOptions in doc := Seq(),
      //javacOptions in doc := Seq("-source", "1.7"),
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
        Resolver.sonatypeRepo("snapshots"),
        "Spray Releases" at "http://repo.spray.io",
        "Spray Nightlies" at "http://nightlies.spray.io"
      ),
      parallelExecution in Test := false
  )

  import com.typesafe.sbt.SbtAspectj.AspectjKeys._
  lazy val aspectjCompileSettings = Seq(
    // only compile the aspects (no weaving)
    aspectjVersion in Aspectj := "Dependencies.aspectj_version",
    compileOnly in Aspectj := true,
    verbose in Aspectj := true,
    aspectjDirectory in Aspectj <<= crossTarget,
    sourceLevel in Aspectj := "-1.7",

    // add the compiled aspects as products
    products in Compile <++= products in Aspectj
  )

}
