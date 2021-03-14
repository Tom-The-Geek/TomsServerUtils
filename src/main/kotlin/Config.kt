package me.geek.tom.serverutils

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml
import me.geek.tom.serverutils.bot.BotType
import me.geek.tom.serverutils.crashreports.CrashReportHelper
import org.apache.commons.io.FileUtils
import java.nio.file.Path

object DiscordBotSpec : ConfigSpec() {
    val token by required<String>()
    val chatWebhook by required<String>()
    val eventWebhook by required<String>()
    val messageChannel by required<String>()
    val serverIcon by required<String>()

    object AllowedMentions : ConfigSpec() {
        val everyone by required<Boolean>()
        val users by required<Boolean>()
        val roles by required<Boolean>()
    }
}

object MiscSpec : ConfigSpec() {
    val profileEndpoint by required<String>()
}

object GeneralSpec : ConfigSpec() {
    val crashReportsEnabled by required<Boolean>()
    val mode by required<BotType>()
}

object CrashReportSpec : ConfigSpec() {
    val webhook by required<String>()
    val webhookIcon by required<String>()
    val webhookName by required<String>()
    val serverName by required<String>()
}

object HomesSpec : ConfigSpec() {
    val enabled by required<Boolean>()
    val allowCrossDimension by required<Boolean>()
    val maxHomeAmount by required<Int>()
    val maxHomesPerDimension by required<Boolean>()
}

fun loadConfig(configDir: Path): Config {
    val configFile = configDir.resolve("serverutils.toml").toFile()
    if (!configFile.exists()) {
        FileUtils.copyInputStreamToFile(
                Thread.currentThread().contextClassLoader.getResource("serverutils_default.toml")?.openStream(), configFile)
    }

    return Config {
        addSpec(DiscordBotSpec)
        addSpec(CrashReportSpec)
        addSpec(HomesSpec)
        addSpec(MiscSpec)
        addSpec(GeneralSpec) }
            .from.toml.resource("serverutils_default.toml")
            .from.toml.file(configFile)
}

class HomesConfig(
        private val config: Config
) {
    val enabled get() = config[HomesSpec.enabled]
    val allowCrossDimension get() = config[HomesSpec.allowCrossDimension]
    val maxHomeAmount get() = config[HomesSpec.maxHomeAmount]
    val maxHomesPerDimension get() = config[HomesSpec.maxHomesPerDimension]
}

class ChatFilterConfig(
    private val config: ConfigSpec
) {

}

fun loadCrashHelper(config: Config): CrashReportHelper {
    if (!config[GeneralSpec.crashReportsEnabled]) {
        return CrashReportHelper.Noop()
    }
    return CrashReportHelper.Impl(
            config[CrashReportSpec.webhook],
            config[CrashReportSpec.webhookName],
            config[CrashReportSpec.webhookIcon],
            config[CrashReportSpec.serverName]
    )
}
