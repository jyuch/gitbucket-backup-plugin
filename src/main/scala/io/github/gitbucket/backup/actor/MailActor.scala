package io.github.gitbucket.backup.actor

import akka.actor.{Actor, ActorLogging}
import gitbucket.core.service.SystemSettingsService
import io.github.gitbucket.backup.service.PluginSettingsService
import org.apache.commons.mail.{DefaultAuthenticator, SimpleEmail}

class MailActor extends Actor with ActorLogging with SystemSettingsService with PluginSettingsService {

  import MailActor._

  private val system = loadSystemSettings()
  private val plugin = loadPluginSettings()

  def receive: Receive = {
    case TestMail() =>
      send(
        "Test mail from Backup Plugin for Windows",
        "If you seen this message, mail settings has worked well.")

    case BackupSuccess() =>
      if (plugin.notifyOnSuccess) {
        send(
          "Backup complete",
          "The backup ended normally.")
      }

    case BackupFailure(msg) =>
      if (plugin.notifyOnFailure) {
        send(
          "Backup failure",
          msg)
      }
  }

  def send(subject: String, body: String) : Unit = {

    plugin.notifyDestination.foreach { dest =>
      if (dest.nonEmpty) {
        system.smtp.foreach { smtp =>
          val email = new SimpleEmail
          email.setHostName(smtp.host)
          smtp.port.foreach(email.setSmtpPort)

          for {
            user <- smtp.user
            pass <- smtp.password
          } {
            email.setAuthenticator(new DefaultAuthenticator(user, pass))
          }

          smtp.ssl.foreach(email.setSSLOnConnect)
          smtp.fromAddress.foreach(email.setFrom)
          email.setSubject(subject)
          email.setMsg(body)
          dest.foreach(email.addTo)
          email.send()
        }
      }
    }
  }
}

object MailActor {

  case class BackupSuccess()

  case class BackupFailure(stackTrace: String)

  case class TestMail()

}