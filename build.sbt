import com.typesafe.sbt.packager.docker._
import sbt.Keys.mappings

name := "iotKafkaAirLightTemp"

version := "0.2.2"

scalaVersion := "2.12.7"

// needed for the fat jar
mainClass in assembly := Some("com.urdnot.iot.KafkaReader")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0-RC1",
  "com.typesafe.play" %% "play" % "2.6.15",
  "com.typesafe.akka" %%"akka-http" % "10.1.7",
  "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
)

enablePlugins(DockerPlugin)

skip in publish := true
// remove application.conf
mappings in(Compile, packageBin) ~= {
  _.filterNot {
    case (_, name) => Seq("application.conf").contains(name)
  }
}
// name the assembly jar
// remember to add the static name to the run-app.sh

assemblyJarName := s"${name.value}.v${version.value}.jar"

// remove other application.confs, properties, reference files from the fat jar
val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case n if n.endsWith(".properties") => MergeStrategy.concat
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("resources/application.conf") => MergeStrategy.discard
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}




dockerBuildOptions += "--no-cache"
dockerUpdateLatest := true
dockerPackageMappings in Docker += file(s"target/scala-2.12/${assemblyJarName.value}") -> s"opt/docker/${assemblyJarName.value}"
mappings in Docker += file("bin/run-app.sh") -> "opt/docker/run-app.sh"
mappings in Docker += file("src/main/resources/application.conf") -> "opt/docker/application.conf"
dockerCommands := Seq(
  Cmd("FROM", "java:8"),
  Cmd("FROM", "anapsix/alpine-java"),
  Cmd("COPY", s"opt/docker/${assemblyJarName.value}", "/home/appuser/lib/iotKafkaAirLightTemp.jar"),
  Cmd("COPY", "opt/docker/run-app.sh", "/var/run-app.sh"),
  Cmd("COPY", "opt/docker/application.conf", "/var/application.conf"),
  Cmd("USER", "root"),
  Cmd("ENV", "JAVA_OPTS=\"-Xmx4G -Xms1G -XX:+UseG1GC\""),
  Cmd("RUN", "chmod 0544 /var/run-app.sh"),
  Cmd("ENTRYPOINT", "/var/run-app.sh")
)
