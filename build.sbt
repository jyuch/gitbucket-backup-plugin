name := "gitbucket-backup-plugin"
organization := "io.github.gitbucket"
version := "1.3.0-SNAPSHOT"
scalaVersion := "2.13.0"
gitbucketVersion := "4.34.0"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.13",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.1034",
  "com.typesafe.akka" %% "akka-actor" % "2.5.31",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.1-akka-2.5.x" exclude("com.mchange", "c3p0") exclude("com.zaxxer", "HikariCP-java6")
)

scalacOptions ++= Seq("-deprecation", "-feature")