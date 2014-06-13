object BuildSettings {
  import sbt._
  import Keys._
  import org.scalastyle.sbt.ScalastylePlugin
  import sbtunidoc.Plugin.UnidocKeys._

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
      javacOptions in (Compile, compile) ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked", "-Xlint:deprecation", "-Xlint:-options"),
      javacOptions in doc := Seq(),
      javacOptions in unidoc := Seq(),
      javaOptions += "-Xmx2G",
      outputStrategy := Some(StdoutOutput),
      // fork := true,
      maxErrors := 1,
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
  import com.typesafe.sbt.SbtAspectj.{ Aspectj, aspectjSettings }
  lazy val aspectjCompileSettings = aspectjSettings ++ Seq(
    // only compile the aspects (no weaving)
    aspectjVersion in Aspectj := Dependencies.aspectj_version,
    compileOnly in Aspectj := true,
    verbose in Aspectj := true,
    aspectjDirectory in Aspectj <<= crossTarget,
    sourceLevel in Aspectj := "-1.6",

    // add the compiled aspects as products
    products in Compile <++= products in Aspectj
  )

}
