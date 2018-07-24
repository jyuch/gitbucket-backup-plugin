package io.github.gitbucket.backup.service

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import io.github.gitbucket.backup.Directory
import io.github.gitbucket.backup.actor.BackupActor
import io.github.gitbucket.backup.actor.BackupActor.{DoBackup, SendTestMail}

trait ActorService {

  import ActorService._

  def initialize(): Unit = {
    scheduler.schedule("Backup", backupActor, BackupActor.DoBackup())
  }

  def teardown(): Unit = {
    system.terminate()
  }

  def sendTestMail(): Unit = {
    backupActor ! SendTestMail()
  }

  def executeBackup(): Unit = {
    backupActor ! DoBackup()
  }

}

object ActorService {
  private val config = ConfigFactory.parseFile(Directory.BackupConf)
  private val system = ActorSystem("backup", config)
  private val scheduler = QuartzSchedulerExtension(system)
  private val backupActor = system.actorOf(BackupActor.props(), "backup")
}