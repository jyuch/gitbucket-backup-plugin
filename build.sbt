name := "gitbucket-backup-plugin"
organization := "io.github.gitbucket"
version := "1.6.0-SNAPSHOT"
scalaVersion := "2.13.10"
gitbucketVersion := "4.39.0"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.15",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.1034",
  "com.typesafe.akka" %% "akka-actor" % "2.6.20",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.20",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.9.3-akka-2.6.x" exclude("com.mchange", "c3p0") exclude("com.zaxxer", "HikariCP-java6")
)

scalacOptions ++= Seq("-deprecation", "-feature")