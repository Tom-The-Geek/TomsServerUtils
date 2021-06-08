package me.geek.tom.serverutils.discord.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.KoinExtension
import com.kotlindiscord.kord.extensions.utils.hasPermission
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Permission
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.message.MessageCreateEvent
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import me.geek.tom.serverutils.DiscordBotSpec
import me.geek.tom.serverutils.clickToCopy
import me.geek.tom.serverutils.colour
import me.geek.tom.serverutils.config
import me.geek.tom.serverutils.discord.DiscordCommandOutput
import me.geek.tom.serverutils.discord.MentionToMinecraftRenderer
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Util
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import org.koin.core.component.inject

class MinecraftExtension(
    bot: ExtensibleBot,
) : KoinExtension(bot) {
    override val name = "Minecraft"

    private val minecraftSerializer = MinecraftSerializer(
        MinecraftSerializerOptions.defaults()
        .addRenderer(MentionToMinecraftRenderer(bot)))

    private val server: MinecraftServer by inject()

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                it.message.channel.id.asString == config!![DiscordBotSpec.messageChannel]
                        && it.message.author?.isBot == false
                        && !it.message.content.startsWith(bot.messageCommands.getPrefix(it))
            }

            action {
                val message = minecraftSerializer.serialize(event.message.content)

                val username = Component.text("@" + (event.member?.nickname ?: event.message.author?.username))
                    .color(TextColor.color(event.member?.colour() ?: 0xFFFFFF))
                    .clickToCopy("Copy mention", "<@${event.message.author?.id?.value}>")

                val minecraftMessage = FabricServerAudiences.of(server).toNative(
                    Component.join(Component.text(": "), username, message)
                )

                server.submit {
                    server.playerManager?.broadcastChatMessage(minecraftMessage, MessageType.CHAT, Util.NIL_UUID)
                }
            }
        }

        if (config!![DiscordBotSpec.Commands.enableIngameCommands]) {
            event<MessageCreateEvent> {
                check {
                    it.message.channel.id.asString == config!![DiscordBotSpec.messageChannel]
                            && it.message.author?.isBot == false
                            && it.message.content.startsWith(bot.messageCommands.getPrefix(it) + "/")
                }

                action {
                    val command = event.message.content.substring(bot.messageCommands.getPrefix(event).length + 1)
                    val source = getCommandSource(server, event.message)
                    server.submit {
                        // TODO: Actually fix this
                        if (command == "stop" && source.hasPermissionLevel(4)) {
                            source.sendError(LiteralText("Warning: this may cause the server to hang on stop due to a bug that I haven't fixed yet - sorry"))
                        }
                        server.commandManager.execute(source, command)
                    }
                }
            }
        }

        if (config!![DiscordBotSpec.Commands.enableList]) {
            command {
                name = "players"
                description = "Show a list of the players on the server"
                aliases = arrayOf(
                    "list",
                    "online"
                )

                action {
                    val players = server.playerManager
                    val playerList = players.playerNames.joinToString(separator = ", ")
                    message.respond("There are ${players.currentPlayerCount}/${players.maxPlayerCount} online: $playerList")
                }
            }
        }
    }

    private suspend fun getCommandSource(server: MinecraftServer, message: Message): ServerCommandSource {
        val overworld = server.overworld
        return ServerCommandSource(
            DiscordCommandOutput(message.channel),
            if (overworld == null) Vec3d.ZERO else Vec3d.of(overworld.spawnPos),
            Vec2f.ZERO,
            overworld,
            getDiscordOpLevel(message.getAuthorAsMember()),
            "Discord",
            LiteralText("Discord"),
            server,
            null
        )
    }

    private suspend fun getDiscordOpLevel(member: Member?): Int {
        return if (member?.hasPermission(Permission.ManageGuild) == true
                || member?.hasPermission(Permission.Administrator) == true) {
            4
        } else {
            0
        }
    }
}
