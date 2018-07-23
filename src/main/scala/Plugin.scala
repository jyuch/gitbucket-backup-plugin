import io.github.gitbucket.helloworld.controller.HelloWorldController
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "backup"
  override val pluginName: String = "Backup Plugin"
  override val description: String = "Provides data backup for GitBucket"
  override val versions: List[Version] = List(new Version("1.0.0"))

  override val controllers = Seq(
    "/*" -> new HelloWorldController()
  )
}
