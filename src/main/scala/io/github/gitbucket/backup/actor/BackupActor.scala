package io.github.gitbucket.backup.actor

import java.io.File
import akka.actor.{Actor, Props}
import akka.pattern._
import akka.util.Timeout
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.actor.DatabaseAccessActor.DumpDatabase
import io.github.gitbucket.backup.actor.FinishingActor.Finishing
import io.github.gitbucket.backup.actor.MailActor.TestMail
import io.github.gitbucket.backup.actor.RepositoryCloneActor.Clone
import io.github.gitbucket.backup.service.PluginSettingsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class BackupActor extends Actor with PluginSettingsService {

  import BackupActor._

  private val mailer = context.actorOf(Props[MailActor](), "mailer")
  private val db = context.actorOf(DatabaseAccessActor.props(mailer), "db")
  private val cloner = context.actorOf(RepositoryCloneActor.props(mailer), "cloner")
  private val packer = context.actorOf(FinishingActor.props(mailer), "packer")

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
          packer ! Finishing(tempBackupDir.getAbsolutePath, backupName)
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