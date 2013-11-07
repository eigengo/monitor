resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.9.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.7.1")