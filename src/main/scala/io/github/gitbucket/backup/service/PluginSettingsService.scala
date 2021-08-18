package io.github.gitbucket.backup.service

import com.typesafe.config.ConfigFactory
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.service.PluginSettingsService.PluginSettings
import io.github.gitbucket.backup.util.ConfigHelper._

trait PluginSettingsService {
  def loadPluginSettings(): PluginSettings = {
    val config = ConfigFactory.parseFile(Directory.BackupConf)
    PluginSettings(
      config.getOptionalString("backup.archive-destination"),
      config.getOptionalInt("backup.archive-limit"),
      config.getOptionalBoolean("backup.notify-on-success").getOrElse(false),
      config.getOptionalBoolean("backup.notify-on-failure").getOrElse(false),
      config.getOptionalStringList("backup.notify-dest"),
      config.getOptionalString("backup.s3.endpoint"),
      config.getOptionalString("backup.s3.region"),
      config.getOptionalString("backup.s3.access-key"),
      config.getOptionalString("backup.s3.secret-key"),
      config.getOptionalString("backup.s3.bucket"),
      config.getOptionalInt("backup.s3.archive-limit")
    )
  }
}

object PluginSettingsService {

  case class PluginSettings(
      archiveDestination: Option[String],
      archiveLimit: Option[Int],
      notifyOnSuccess: Boolean,
      notifyOnFailure: Boolean,
      notifyDestination: Option[List[String]],
      endpoint: Option[String],
      region: Option[String],
      accessKey: Option[String],
      secretKey: Option[String],
      bucket: Option[String],
      s3ArchiveLimit: Option[Int])

}
