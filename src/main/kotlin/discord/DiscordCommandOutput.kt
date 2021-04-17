package me.geek.tom.serverutils.discord

import dev.kord.core.behavior.channel.TextChannelBehavior
import kotlinx.coroutines.runBlocking
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import java.util.*

class DiscordCommandOutput(
    private val channel: TextChannelBehavior,
) : CommandOutput {
    override fun sendSystemMessage(message: Text, senderUuid: UUID) {
        runBlocking {
            channel.createMessage(message.string)
        }
    }

    override fun shouldReceiveFeedback(): Boolean {
        return true
    }

    override fun shouldTrackOutput(): Boolean {
        return true
    }

    override fun shouldBroadcastConsoleToOps(): Boolean {
        return false
    }
}
