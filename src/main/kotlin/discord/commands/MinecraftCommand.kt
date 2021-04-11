package me.geek.tom.serverutils.discord.commands

import me.geek.tom.serverutils.discord.DiscordCommandManager
import me.geek.tom.serverutils.discord.DiscordCommandOutput
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object MinecraftCommand : DiscordCommand {
    override fun execute(server: MinecraftServer, message: Message) {
        val hasPermission = message.member?.hasPermission(Permission.MANAGE_SERVER)
        if (hasPermission != true) {
            message.reply("You do not have permission to run in-game commands!").queue()
            return
        }
        val command = message.contentRaw.substring(DiscordCommandManager.prefix.length + 1)
        server.execute {
            server.commandManager.execute(getCommandSource(server, message.channel), command)
        }
    }

    /**
     * Basically just [MinecraftServer.getCommandSource] but with a custom output
     */
    private fun getCommandSource(server: MinecraftServer, channel: MessageChannel): ServerCommandSource {
        val overworld = server.overworld
        return ServerCommandSource(
            DiscordCommandOutput(channel),
            if (overworld == null) Vec3d.ZERO else Vec3d.of(overworld.spawnPos),
            Vec2f.ZERO,
            overworld,
            4,
            "Discord",
            LiteralText("Discord"),
            server,
            null
        )
    }
}
