package me.geek.tom.serverutils.discord.extensions

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.KoinExtension
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.core.event.message.MessageCreateEvent
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import me.geek.tom.serverutils.DiscordBotSpec
import me.geek.tom.serverutils.clickToCopy
import me.geek.tom.serverutils.colour
import me.geek.tom.serverutils.config
import me.geek.tom.serverutils.discord.MentionToMinecraftRenderer
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Util
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
