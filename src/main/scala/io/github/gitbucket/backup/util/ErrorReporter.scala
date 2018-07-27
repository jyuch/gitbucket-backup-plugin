package io.github.gitbucket.backup.util

import java.io.{PrintWriter, StringWriter}

import akka.actor.{Actor, ActorRef}
import io.github.gitbucket.backup.actor.MailActor.BackupFailure

trait ErrorReporter {

  val mailer: Option[ActorRef]

  def reportError(reason: Throwable): Unit = {
    mailer foreach { actor =>
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      reason.printStackTrace(pw)
      actor ! BackupFailure(sw.toString)
    }
  }
}
