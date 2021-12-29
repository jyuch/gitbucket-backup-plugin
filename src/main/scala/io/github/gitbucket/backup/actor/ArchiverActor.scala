package io.github.gitbucket.backup.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.service.PluginSettingsService
import io.github.gitbucket.backup.util.ErrorReporter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.zeroturnaround.zip.ZipUtil

import scala.jdk.CollectionConverters._

class ArchiverActor(mail: ActorRef) extends Actor with ActorLogging with PluginSettingsService with ErrorReporter {

  import ArchiverActor._

  private val config = loadPluginSettings()

  override val mailer: Option[ActorRef] = Some(mail)

  override def receive: Receive = {
    case Archive(baseDir, backupName) =>
      val tempBackupDir = new File(baseDir)

      val srcDataDir = new File(gDirectory.DatabaseHome)
      val data = Directory.getDataBackupDir(tempBackupDir)
      if (srcDataDir.exists()) {
        FileUtils.copyDirectory(srcDataDir, data)
      }

      val zip = new File(config.archiveDestination.getOrElse(gDirectory.GitBucketHome), s"$backupName.zip")
      ZipUtil.pack(tempBackupDir, zip)

      config.archiveLimit foreach { n =>
        if (n > 0) {
          val pattern = """^backup-\d{12}\.zip$""".r
          val d = new File(config.archiveDestination.getOrElse(gDirectory.GitBucketHome))

          val t = d.listFiles.filter(
            _.getName match {
              case pattern() => true
              case _ => false
            }).sortBy(_.getName).reverse.drop(n)

          t foreach { f =>
            log.info("Delete archive {}", f.getAbsoluteFile)
            f.delete()
          }
        }
      }

      val tempDirectoryEntries = FileUtils.iterateFiles(tempBackupDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).asScala
      for (it <- tempDirectoryEntries) {
        // In Windows, can't delete temp dir because index file marked as readonly.
        it.setWritable(true)
      }
      FileUtils.deleteDirectory(tempBackupDir)

      sender() ! ((): Unit)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    reportError(reason)
    super.preRestart(reason, message)
  }
}

object ArchiverActor {
  def props(mailer: ActorRef): Props = {
    Props[ArchiverActor](new ArchiverActor(mailer))
  }

  sealed case class Archive(baseDir: String, backupName: String)

}