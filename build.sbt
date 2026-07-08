import java.io.File

def loadDotEnv(path: String = ".env"): Map[String, String] = {
  val file = new File(path)
  if (file.exists()) {
    val lines = scala.io.Source.fromFile(file).getLines().toList
    lines
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .flatMap { line =>
        line.split("=", 2) match {
          case Array(k, v) => Some(k.trim -> v.trim)
          case _           => None
        }
      }
      .toMap
  } else {
    Map.empty
  }
}

val dotEnv = loadDotEnv()

val java11Home: Option[String] = dotEnv.get("JAVA11_HOME").orElse(sys.env.get("JAVA11_HOME"))

val warnIfMissingJavaHome: Unit = if (java11Home.isEmpty) {
  println("[build.sbt] ATTENTION: JAVA11_HOME non trouvé (ni dans .env, ni dans l'environnement). " +
    "Spark risque de rencontrer le bug de compatibilité Java 17. " +
    "Crée un fichier .env à la racine avec: JAVA11_HOME=/chemin/vers/ton/jdk11")
}

lazy val root = (project in file(".")).

  settings(
    inThisBuild(List(
      organization := "example",
      scalaVersion := "2.12.13"
    )),
    name := "sparkScala",
    version := "0.0.1",

    javaHome := java11Home.map(file(_)),

    javaOptions ++= Seq(
      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED"
    ),

    javaOptions ++= Seq("-Xms512M", "-Xmx2048M"),
    parallelExecution in Test := false,
    fork := true,

    coverageHighlighting := true,

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-streaming" % "3.5.1" % "provided",
      "org.apache.spark" %% "spark-sql" % "3.5.1" % "provided",

      "com.microsoft.onnxruntime" % "onnxruntime" % "1.18.0",

      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
    ),

    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,

    runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in (Compile, run)).evaluated,

    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    pomIncludeRepository := { x => false },

   resolvers ++= Seq(
      "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
      "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
      "Second Typesafe repo" at "https://repo.typesafe.com/typesafe/maven-releases/",
      Resolver.sonatypeRepo("public")
    ),


    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )
