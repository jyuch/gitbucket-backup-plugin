package io.github.gitbucket.backup.util

import com.typesafe.config.Config

object ConfigHelper {

  implicit class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }

    def getOptionalInt(path: String): Option[Int] = {
      if (underlying.hasPath(path)) {
        Some(underlying.getInt(path))
      } else {
        None
      }
    }

    def getOptionalBoolean(path: String): Option[Boolean] = {
      if (underlying.hasPath(path)) {
        Some(underlying.getBoolean(path))
      } else {
        None
      }
    }

    def getOptionalStringList(path: String): Option[List[String]] = {
      import collection.JavaConverters._
      if (underlying.hasPath(path)) {
        Some(underlying.getStringList(path).asScala.toList)
      } else {
        None
      }
    }
  }
}
