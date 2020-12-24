package me.geek.tom.serverutils.chatfilter

import net.minecraft.server.MinecraftServer
import java.nio.file.Path

interface ChatFilter {

    fun initialise(configDir: Path, server: MinecraftServer)

    /**
     * Checks the given [message] and return `false` to indicate the message failed processing.
     * This will be called on a helper thread to avoid blocking
     */
    fun checkChatMessage(message: String): Boolean

    /**
     * A friendly name for the filter.
     */
    fun getName(): String
}
