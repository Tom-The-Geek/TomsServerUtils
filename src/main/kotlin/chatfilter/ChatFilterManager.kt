package me.geek.tom.serverutils.chatfilter

import me.geek.tom.serverutils.chatfilter.filters.ScriptFilter
import net.minecraft.server.MinecraftServer
import java.nio.file.Files
import java.nio.file.Path

class ChatFilterManager(
    private val filters: MutableList<ChatFilter> = ArrayList(),
) {
    fun init(configDir: Path, server: MinecraftServer) {
        val scriptsDir = configDir.resolve("scripts")
        if (!Files.exists(scriptsDir)) {
            Files.createDirectories(scriptsDir)
        }
        Files.list(scriptsDir)
            .filter { Files.isRegularFile(it) }
            .filter { it.fileName.toString().endsWith(".json") }
            .forEach {
                filters.add(ScriptFilter(it))
            }

        filters.forEach { it.initialise(configDir, server) }
    }

    fun onChatMessage(message: String): List<String> {
        val failed = ArrayList<String>()

        for (filter in filters) {
            if (!filter.checkChatMessage(message)) {
                failed.add(filter.getName())
            }
        }

        return failed
    }
}
