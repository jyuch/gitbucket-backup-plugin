name := "gitbucket-backup-plugin"
organization := "io.github.gitbucket"
version := "1.2.2"
scalaVersion := "2.13.0"
gitbucketVersion := "4.32.0"

libraryDependencies ++= Seq(
  "org.zeroturnaround" % "zt-zip" % "1.13",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.368"
)

scalacOptions ++= Seq("-deprecation", "-feature")