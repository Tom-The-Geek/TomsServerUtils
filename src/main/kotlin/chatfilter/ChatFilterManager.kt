package me.geek.tom.serverutils.chatfilter

import net.minecraft.server.MinecraftServer
import java.nio.file.Path

class ChatFilterManager(
    private val filters: List<ChatFilter>,
) {
    fun init(configDir: Path, server: MinecraftServer) {
        filters.forEach { it.initialise(configDir, server) }
    }

    fun onChatMessage(message: String): Boolean {
        val failed = ArrayList<String>()

        for (filter in filters) {
            if (!filter.checkChatMessage(message)) {
                failed.add(filter.getName())
            }
        }

        return failed.isEmpty()
    }
}
