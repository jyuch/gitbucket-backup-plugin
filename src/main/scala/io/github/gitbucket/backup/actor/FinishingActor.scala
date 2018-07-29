package io.github.gitbucket.backup.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectRequest
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.backup.Directory
import io.github.gitbucket.backup.actor.MailActor.BackupSuccess
import io.github.gitbucket.backup.service.PluginSettingsService
import io.github.gitbucket.backup.util.ErrorReporter
import org.apache.commons.io.FileUtils
import org.zeroturnaround.zip.ZipUtil

class FinishingActor(mail: ActorRef) extends Actor with ActorLogging with PluginSettingsService with ErrorReporter {

  import FinishingActor._

  private val config = loadPluginSettings()

  override val mailer: Option[ActorRef] = Some(mail)

  override def receive: Receive = {
    case Finishing(baseDir, backupName) =>
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

      val s3Config = for {
        endpoint <- config.endpoint
        region <- config.region
        accessKey <- config.accessKey
        secretKey <- config.secretKey
        bucket <- config.bucket
      } yield S3Config(endpoint, region, accessKey, secretKey, bucket)

      s3Config foreach { s3 =>
        val credentials = new BasicAWSCredentials(s3.accessKey, s3.secretKey)
        val clientConfiguration = new ClientConfiguration
        clientConfiguration.setSignerOverride("AWSS3V4SignerType")

        val s3Client = AmazonS3ClientBuilder
          .standard()
          .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3.endpoint, s3.region))
          .withPathStyleAccessEnabled(true)
          .withClientConfiguration(clientConfiguration)
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .build()

        s3Client.putObject(new PutObjectRequest(s3.bucket, zip.getName, zip))

        log.info("Upload to Object storage complete")
      }

      FileUtils.deleteDirectory(tempBackupDir)

      log.info("Backup complete")
      mail ! BackupSuccess()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    reportError(reason)
    super.preRestart(reason, message)
  }
}

object FinishingActor {
  def props(mailer: ActorRef): Props = {
    Props[FinishingActor](new FinishingActor(mailer))
  }

  sealed case class Finishing(baseDir: String, backupName: String)

  case class S3Config(endpoint: String,
                      region: String,
                      accessKey: String,
                      secretKey: String,
                      bucket: String)

}