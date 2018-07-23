package io.github.gitbucket.backup

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import gitbucket.core.util.{Directory => gDirectory}

object Directory {
  val BackupConf = new File(gDirectory.GitBucketHome, "backup.conf")

  def getBackupName: String = {
    val now = LocalDateTime.now
    val f = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    s"backup-${f.format(now)}"
  }

  def getRepositoryBackupDir(baseDir: File, user: String, repoName: String): File = {
    new File(new File(new File(baseDir, "repositories"), user), s"${repoName}.git")
  }

  def getWikiBackupDir(baseDir: File, user: String, repoName: String): File = {
    new File(new File(new File(baseDir, "repositories"), user), s"${repoName}.wiki.git")
  }

  def getRepositoryFilesBackupDir(baseDir: File, user: String, repoName: String): File = {
    new File(new File(new File(baseDir, "repositories"), user), repoName)
  }

  def getDataBackupDir(baseDir: File): File = {
    new File(baseDir, "data")
  }
}
