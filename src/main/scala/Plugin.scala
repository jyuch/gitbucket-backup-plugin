import io.github.gitbucket.helloworld.controller.HelloWorldController
import io.github.gitbucket.solidbase.model.Version

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "helloworld"
  override val pluginName: String = "HelloWorld Plugin"
  override val description: String = "First example of GitBucket plug-in"
  override val versions: List[Version] = List(new Version("1.0.0"))

  override val controllers = Seq(
    "/*" -> new HelloWorldController()
  )
}
