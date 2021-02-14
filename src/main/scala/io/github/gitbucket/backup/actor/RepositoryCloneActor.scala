package io.github.gitbucket.backup.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import gitbucket.core.util.{JGitUtil, Directory => gDirectory}
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.util.ErrorReporter
import org.apache.commons.io.FileUtils

class RepositoryCloneActor(mail: ActorRef) extends Actor with ActorLogging with ErrorReporter {

  import RepositoryCloneActor._

  override val mailer: Option[ActorRef] = Some(mail)

  override def receive: Receive = {
    case Clone(baseDir, user, repo) =>
      val src = gDirectory.getRepositoryDir(user, repo)
      val dest = Directory.getRepositoryBackupDir(new File(baseDir), user, repo)
      JGitUtil.cloneRepository(src, dest)

      val wikiSrc = gDirectory.getWikiRepositoryDir(user, repo)
      val wikiDest = Directory.getWikiBackupDir(new File(baseDir), user, repo)
      JGitUtil.cloneRepository(wikiSrc, wikiDest)

      val filesSrc = gDirectory.getRepositoryFilesDir(user, repo)
      val filesDest = Directory.getRepositoryFilesBackupDir(new File(baseDir), user, repo)
      if (filesSrc.exists() && filesSrc.isDirectory) {
        FileUtils.copyDirectory(filesSrc, filesDest)
      }

      log.info("Clone repository {}/{}", user, repo)
      sender() ! ((): Unit)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    reportError(reason)
    super.preRestart(reason, message)
  }
}

object RepositoryCloneActor {
  def props(mailer: ActorRef): Props = {
    Props[RepositoryCloneActor](new RepositoryCloneActor(mailer))
  }

  sealed case class Clone(baseDir: String, user: String, repo: String)

}