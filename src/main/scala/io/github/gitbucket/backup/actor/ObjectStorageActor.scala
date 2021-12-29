package io.github.gitbucket.backup.actor

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.PutObjectRequest
import gitbucket.core.util.{Directory => gDirectory}
import io.github.gitbucket.backup.service.PluginSettingsService
import io.github.gitbucket.backup.util.ErrorReporter

import scala.jdk.CollectionConverters._

class ObjectStorageActor(mail: ActorRef) extends Actor with ActorLogging with PluginSettingsService with ErrorReporter {

  import ObjectStorageActor._

  private val config = loadPluginSettings()

  override val mailer: Option[ActorRef] = Some(mail)

  override def receive: Receive = {
    case PutArchive(backupName) =>
      val zip = new File(config.archiveDestination.getOrElse(gDirectory.GitBucketHome), s"$backupName.zip")

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

        config.s3ArchiveLimit foreach { n =>
          if (n > 0) {
            val pattern = """^backup-\d{12}\.zip$""".r
            val objects = s3Client.listObjects(s3.bucket).getObjectSummaries.asScala
            val t = objects.filter(
              _.getKey match {
                case pattern() => true
                case _ => false
              }).map(_.getKey).sorted.reverse.drop(n)

            t foreach { o =>
              log.info("Delete object {} in bucket", o)
              s3Client.deleteObject(s3.bucket, o)
            }
          }
        }
      }
      sender() ! ((): Unit)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    reportError(reason)
    super.preRestart(reason, message)
  }
}

object ObjectStorageActor {
  def props(mailer: ActorRef): Props = {
    Props[ObjectStorageActor](new ObjectStorageActor(mailer))
  }

  sealed case class PutArchive(backupName: String)

  case class S3Config(
      endpoint: String,
      region: String,
      accessKey: String,
      secretKey: String,
      bucket: String)

}