import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.service.SystemSettingsService
import io.github.gitbucket.backup.controller.MainController
import io.github.gitbucket.backup.service.ActorService
import io.github.gitbucket.solidbase.model.Version
import javax.servlet.ServletContext
import org.slf4j.LoggerFactory

class Plugin extends gitbucket.core.plugin.Plugin with ActorService {
  override val pluginId: String = "backup"
  override val pluginName: String = "Backup Plugin"
  override val description: String = "Provides all in one backup features for GitBucket"
  override val versions: List[Version] = List(new Version("1.0.0"))

  private val logger = LoggerFactory.getLogger(classOf[Plugin])

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    super.initialize(registry, context, settings)
    initialize()
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    teardown()
    logger.info("{} is shutting down.", pluginName)
  }

  override val controllers = Seq(
    "/*" -> new MainController()
  )
}
