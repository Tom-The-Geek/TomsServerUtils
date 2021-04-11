package me.geek.tom.serverutils.discord.commands

import net.dv8tion.jda.api.entities.Message
import net.minecraft.server.MinecraftServer

interface DiscordCommand {
    fun execute(server: MinecraftServer, message: Message)
}
