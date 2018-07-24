package io.github.gitbucket.backup.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.AdminAuthenticator
import io.github.gitbucket.backup.service.ActorService
import org.scalatra.Ok

class MainController extends ControllerBase with AdminAuthenticator with ActorService {

  post("/api/v3/back/mail-test") {
    adminOnly {
      sendTestMail()
      Ok()
    }
  }

  post("/api/v3/back/execute") {
    adminOnly {
      executeBackup()
      Ok()
    }
  }
}
