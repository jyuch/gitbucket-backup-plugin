package io.github.gitbucket.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.AdminAuthenticator
import org.scalatra.Ok

class MainController extends ControllerBase with AdminAuthenticator {

  post("/api/v3/backup/execute-backup") {
    adminOnly {
      Ok()
    }
  }
}
