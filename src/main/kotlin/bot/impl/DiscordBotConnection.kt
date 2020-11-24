package me.geek.tom.serverutils.bot.impl

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer
import me.geek.tom.serverutils.DiscordBotSpec
import me.geek.tom.serverutils.MiscSpec
import me.geek.tom.serverutils.bot.BotConnection
import me.geek.tom.serverutils.ducks.IPlayerAccessor
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.platform.fabric.FabricAudiences
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.PlayerManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util

class DiscordBotConnection(private val config: Config) : BotConnection, ListenerAdapter() {

    private var jda: JDA? = null
    private var webhookClient: WebhookClient? = null
    private var playerManager: PlayerManager? = null

    override fun connect(server: MinecraftServer) {
        this.jda = JDABuilder.createDefault(this.config[DiscordBotSpec.token])
                .addEventListeners(this)
                .build()
        this.webhookClient = WebhookClient.withUrl(this.config[DiscordBotSpec.webhook])
        this.playerManager = server.playerManager
    }

    override fun disconnect() {
        if (jda != null) {
            jda!!.shutdown()
            jda = null
        }
        if (webhookClient != null) {
            webhookClient!!.close()
            webhookClient = null
        }
        if (this.playerManager != null) {
            this.playerManager = null
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.channel.id != this.config[DiscordBotSpec.messageChannel] || event.author.isBot) return

        val message = MinecraftSerializer.INSTANCE.serialize(event.message.contentRaw)
        val username = Component.text(event.member?.nickname?: event.author.name)
                .color(TextColor.color(event.member?.colorRaw?: 0xFFFFFF))
        val minecraftMessage = FabricServerAudiences.of(this.playerManager!!.server).toNative(
                Component.join(Component.text(": "), username, (message)))
        this.playerManager!!.broadcastChatMessage(minecraftMessage, MessageType.CHAT, Util.NIL_UUID)
    }

    override fun onChatMessage(user: GameProfile, headOverlay: Boolean, message: String) {
        val webhookMessage = WebhookMessageBuilder()
                .setUsername(user.name)
                .setAvatarUrl(createAvatarUrl(user, headOverlay))
                .setContent(message)
                .build()
        this.webhookClient!!.send(webhookMessage)
    }

    private fun createAvatarUrl(user: GameProfile, headOverlay: Boolean) =
            this.config[MiscSpec.profileEndpoint] + user.id + "?overlay=" + headOverlay

    override fun onPlayerJoin(player: ServerPlayerEntity) {
        val overlay = (player as IPlayerAccessor).serverutils_showHat()
        val avatarUrl = createAvatarUrl(player.gameProfile, overlay)
        val webhookMessage = WebhookMessageBuilder()
                .setUsername("Server")
                .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
                .addEmbeds(WebhookEmbedBuilder()
                        .setColor(0x00FF00)
                        .setDescription("")
                        .setAuthor(WebhookEmbed.EmbedAuthor("${player.gameProfile.name} joined the game!", avatarUrl, ""))
                        .build())
                .build()
        this.webhookClient!!.send(webhookMessage)
    }

    override fun onPlayerLeave(player: ServerPlayerEntity) {
        val overlay = (player as IPlayerAccessor).serverutils_showHat()
        val avatarUrl = createAvatarUrl(player.gameProfile, overlay)
        val webhookMessage = WebhookMessageBuilder()
                .setUsername("Server")
                .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
                .addEmbeds(WebhookEmbedBuilder()
                        .setDescription("")
                        .setAuthor(WebhookEmbed.EmbedAuthor("${player.gameProfile.name} left the game!", avatarUrl, ""))
                        .setColor(0xFF0000)
                        .build())
                .build()
        this.webhookClient!!.send(webhookMessage)
    }
}
