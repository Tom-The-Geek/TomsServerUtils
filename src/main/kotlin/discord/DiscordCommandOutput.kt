package me.geek.tom.serverutils.discord

import net.dv8tion.jda.api.entities.MessageChannel
import net.minecraft.server.command.CommandOutput
import net.minecraft.text.Text
import java.util.*

class DiscordCommandOutput(
    private val channel: MessageChannel,
) : CommandOutput {
    override fun sendSystemMessage(message: Text, senderUuid: UUID) {
        channel.sendMessage(message.string).queue()
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
