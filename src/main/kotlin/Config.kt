package me.geek.tom.serverutils

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.toml
import me.geek.tom.serverutils.bot.BotType
import org.apache.commons.io.FileUtils
import java.nio.file.Path

object DiscordBotSpec : ConfigSpec() {
    val token by required<String>()
    val webhook by required<String>()
    val messageChannel by required<String>()
    val serverIcon by required<String>()
}

object MiscSpec : ConfigSpec() {
    val profileEndpoint by required<String>()
}

object GeneralSpec : ConfigSpec() {
    val mode by required<BotType>()
}

fun loadConfig(configDir: Path): Config {
    val configFile = configDir.resolve("serverutils.toml").toFile()
    if (!configFile.exists()) {
        FileUtils.copyInputStreamToFile(
                Thread.currentThread().contextClassLoader.getResource("serverutils_default.toml")?.openStream(), configFile)
    }

    return Config {
        addSpec(DiscordBotSpec)
        addSpec(MiscSpec)
        addSpec(GeneralSpec) }
            .from.toml.resource("serverutils_default.toml")
            .from.toml.file(configFile)
}
