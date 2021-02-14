package io.github.gitbucket.backup.service

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import io.github.gitbucket.backup.util.Directory
import io.github.gitbucket.backup.actor.BackupActor
import io.github.gitbucket.backup.actor.BackupActor.{DoBackup, SendTestMail}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ActorService {

  import ActorService._

  def initialize(): Unit = {
    scheduler.schedule("Backup", backupActor, BackupActor.DoBackup())
  }

  def teardown(): Unit = {
    // Wait actor system shutdown because ClassLoader is closed when exit this method.
    Await.ready(system.terminate(), Duration(1, TimeUnit.MINUTES))
  }

  def sendTestMail(): Unit = {
    backupActor ! SendTestMail()
  }

  def executeBackup(): Unit = {
    backupActor ! DoBackup()
  }

}

object ActorService {
  private val baseConfig = ConfigFactory.load(classOf[ActorService].getClassLoader)
  private val config = ConfigFactory.parseFile(Directory.BackupConf)
  private val system = ActorSystem("backup", config.withFallback(baseConfig), classOf[ActorService].getClassLoader)
  private val scheduler = QuartzSchedulerExtension(system)
  private val backupActor = system.actorOf(BackupActor.props(), "backup")
}