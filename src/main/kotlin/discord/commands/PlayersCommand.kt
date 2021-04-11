package me.geek.tom.serverutils.discord.commands

import net.dv8tion.jda.api.entities.Message
import net.minecraft.server.MinecraftServer

object PlayersCommand : DiscordCommand {
    override fun execute(server: MinecraftServer, message: Message) {
        val players = server.playerManager
        val playerList = players.playerNames.joinToString(separator = ", ")
        message.reply("There are ${players.currentPlayerCount}/${players.maxPlayerCount} online: $playerList").queue()
    }
}
