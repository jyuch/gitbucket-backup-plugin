package io.github.gitbucket.backup.actor

import java.io.File
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.actor.DatabaseAccessActor.DumpDatabase
import io.github.gitbucket.backup.actor.ArchiverActor.Archive
import io.github.gitbucket.backup.actor.MailActor.{BackupSuccess, TestMail}
import io.github.gitbucket.backup.actor.ObjectStorageActor.PutArchive
import io.github.gitbucket.backup.actor.RepositoryCloneActor.Clone
import io.github.gitbucket.backup.service.PluginSettingsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class BackupActor extends Actor with ActorLogging with PluginSettingsService {

  import BackupActor._

  private val mailer = context.actorOf(Props[MailActor](), "mailer")
  private val db = context.actorOf(DatabaseAccessActor.props(mailer), "db")
  private val cloner = context.actorOf(RepositoryCloneActor.props(mailer), "cloner")
  private val archiver = context.actorOf(ArchiverActor.props(mailer), "archiver")
  private val storage = context.actorOf(ObjectStorageActor.props(mailer), "storage")

  private val config = loadPluginSettings()

  override def receive: Receive = {
    case DoBackup() =>
      val backupName = Directory.getBackupName

      val tempBackupDir = new File(gDirectory.GitBucketHome, backupName)
      implicit val timeout: Timeout = Timeout(config.timeoutMinutes minutes)

      val repos = (db ? DumpDatabase(tempBackupDir.getAbsolutePath)).mapTo[List[Clone]]

      repos foreach { r =>
        val c = r.map(cloner ? _)

        Future.sequence(c) foreach { _ =>
          for {
            _ <- archiver ? Archive(tempBackupDir.getAbsolutePath, backupName)
            _ <- storage ? PutArchive(backupName)
          } yield {
            log.info("Backup complete")
            mailer ! BackupSuccess()
          }
        }
      }
    case SendTestMail() =>
      mailer ! TestMail()
  }
}

object BackupActor {

  def props(): Props = {
    Props[BackupActor]()
  }

  sealed case class DoBackup()

  sealed case class SendTestMail()

}