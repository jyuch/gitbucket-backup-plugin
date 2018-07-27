package io.github.gitbucket.backup.actor

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import gitbucket.core.model.Profile.profile.blockingApi._
import gitbucket.core.service.{AccountService, RepositoryService}
import gitbucket.core.servlet.Database
import gitbucket.core.util.JDBCUtil.RichConnection
import io.github.gitbucket.backup.actor.RepositoryCloneActor.Clone
import io.github.gitbucket.backup.util.ErrorReporter
import org.apache.commons.io.FileUtils

class DatabaseAccessActor(mail: ActorRef) extends Actor with AccountService with RepositoryService with ErrorReporter {

  import DatabaseAccessActor._

  override val mailer: Option[ActorRef] = Some(mail)

  override def receive: Receive = {
    case DumpDatabase(baseDir) => {

      Database() withTransaction { implicit session =>
        val allTables = session.conn.allTableNames()
        val sqlFile = session.conn.exportAsSQL(allTables)
        val sqlBackup = new File(baseDir, "gitbucket.sql")
        FileUtils.copyFile(sqlFile, sqlBackup)

        val repos = for {
          user <- getAllUsers()
          repo <- getRepositoryNamesOfUser(user.userName)
        } yield {
          Clone(baseDir, user.userName, repo)
        }
        sender() ! repos
      }
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    reportError(reason)
    super.preRestart(reason, message)
  }
}

object DatabaseAccessActor {
  def props(mailer: ActorRef) : Props = {
    Props[DatabaseAccessActor](new DatabaseAccessActor(mailer))
  }

  sealed case class DumpDatabase(baseDir: String)

}