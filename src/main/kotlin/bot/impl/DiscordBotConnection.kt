package me.geek.tom.serverutils.bot.impl

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import me.geek.tom.serverutils.DiscordBotSpec
import me.geek.tom.serverutils.MiscSpec
import me.geek.tom.serverutils.bot.BotConnection
import me.geek.tom.serverutils.clickToCopy
import me.geek.tom.serverutils.discord.DiscordCommandManager
import me.geek.tom.serverutils.discord.MentionToMinecraftRenderer
import me.geek.tom.serverutils.ducks.IPlayerAccessor
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.MessageType
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Util
import okhttp3.OkHttpClient
import okhttp3.Protocol

class DiscordBotConnection(private val config: Config) : BotConnection, ListenerAdapter() {

    private var jda: JDA? = null
    private var chatWebhookClient: WebhookClient? = null
    private var eventWebhookClient: WebhookClient? = null
    private var server: MinecraftServer? = null
    private var minecraftSerializer: MinecraftSerializer? = null
    private val allowedMentions by lazy {
        AllowedMentions.none()
            .withParseEveryone(this.config[DiscordBotSpec.AllowedMentions.everyone])
            .withParseUsers(this.config[DiscordBotSpec.AllowedMentions.users])
            .withParseRoles(this.config[DiscordBotSpec.AllowedMentions.roles])
    }

    private val commandManager: DiscordCommandManager = DiscordCommandManager(config)

    override fun connect(server: MinecraftServer) {
        this.jda = JDABuilder.createDefault(this.config[DiscordBotSpec.token])
                .addEventListeners(this)
                .build()
        if (this.config[DiscordBotSpec.chatWebhook].isNotEmpty()) {
            this.chatWebhookClient = WebhookClientBuilder(this.config[DiscordBotSpec.chatWebhook])
                .setDaemon(true)
                .setAllowedMentions(allowedMentions)
                .setHttpClient(OkHttpClient.Builder()
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .build())
                .build()
        }
        if (this.config[DiscordBotSpec.eventWebhook].isNotEmpty()) {
            this.eventWebhookClient = WebhookClientBuilder(this.config[DiscordBotSpec.eventWebhook])
                .setDaemon(true)
                .setAllowedMentions(allowedMentions)
                .setHttpClient(OkHttpClient.Builder()
                    .protocols(listOf(Protocol.HTTP_1_1))
                    .build())
                .build()
        }
        this.server = server
        minecraftSerializer = MinecraftSerializer(MinecraftSerializerOptions.defaults()
            .addRenderer(MentionToMinecraftRenderer(jda!!)))
    }

    override fun disconnect() {
        if (jda != null) {
            jda!!.shutdown()
            jda = null
        }
        if (minecraftSerializer != null) {
            minecraftSerializer = null
        }
        if (chatWebhookClient != null) {
            chatWebhookClient!!.close()
            chatWebhookClient = null
        }
        if (eventWebhookClient != null) {
            eventWebhookClient!!.close()
            eventWebhookClient = null
        }
        if (this.server != null) {
            this.server = null
        }
    }

    override fun serverStarting(server: MinecraftServer) {
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(0x00FF00)
                .setAuthor(WebhookEmbed.EmbedAuthor("Server starting...", "", ""))
                .setDescription("")
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    override fun serverStarted(server: MinecraftServer) {
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(0x00FF00)
                .setAuthor(WebhookEmbed.EmbedAuthor("Server started!", "", ""))
                .setDescription("")
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    override fun serverStopping(server: MinecraftServer) {
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setAuthor(WebhookEmbed.EmbedAuthor("Server stopping...", "", ""))
                .setDescription("")
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    override fun serverStopped(server: MinecraftServer) {
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(0xFF0000)
                .setAuthor(WebhookEmbed.EmbedAuthor("Server stopped!", "", ""))
                .setDescription("")
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.channel.id != this.config[DiscordBotSpec.messageChannel] || event.author.isBot) return

        // Try running a command before proxying the chat
        if (commandManager.handleMessage(this.server!!, event.message)) {
            return
        }

        val message = minecraftSerializer!!.serialize(event.message.contentRaw)

        val username = Component.text("@" + (event.member?.nickname?: event.author.name))
            .color(TextColor.color(event.member?.colorRaw?: 0xFFFFFF))
            .clickToCopy("Copy mention", "<@${event.author.idLong}>")

        val minecraftMessage = FabricServerAudiences.of(this.server!!).toNative(
                Component.join(Component.text(": "), username, message))

        this.server?.submit {
            this.server?.playerManager?.broadcastChatMessage(minecraftMessage, MessageType.CHAT, Util.NIL_UUID)
        }
    }

    override fun onReady(event: ReadyEvent) {
        if (config[DiscordBotSpec.presenceEnabled]) {
            event.jda.presence.activity = Activity.watching("over the server!")
        }
    }

    override fun onBroadcast(text: Text) {
        val component = FabricServerAudiences.of(this.server!!).toAdventure(text)
        val message = DiscordSerializer.INSTANCE.serialize(component)
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .setContent(message)
            .build()
        this.chatWebhookClient?.send(webhookMessage)
    }

    override fun onChatMessage(user: GameProfile, headOverlay: Boolean, message: String) {
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername(user.name)
            .setAvatarUrl(createAvatarUrl(user, headOverlay))
            .setContent(message)
            .build()
        this.chatWebhookClient?.send(webhookMessage)
    }

    private fun createAvatarUrl(user: GameProfile, headOverlay: Boolean) =
            this.config[MiscSpec.profileEndpoint] + user.id + "?overlay=" + headOverlay

    override fun onPlayerJoin(player: ServerPlayerEntity) {
        val overlay = (player as IPlayerAccessor).serverutils_showHat()
        val avatarUrl = createAvatarUrl(player.gameProfile, overlay)
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(0x00FF00)
                .setDescription("")
                .setAuthor(WebhookEmbed.EmbedAuthor("${player.gameProfile.name} joined the game!", avatarUrl, ""))
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    // This function is called for player deaths and advancements.
    override fun onPlayerAnnouncement(player: ServerPlayerEntity, message: Text, colour: Int) {
        val overlay = (player as IPlayerAccessor).serverutils_showHat()
        val avatarUrl = createAvatarUrl(player.gameProfile, overlay)
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setColor(colour)
                .setDescription("")
                .setAuthor(WebhookEmbed.EmbedAuthor(message.string, avatarUrl, ""))
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }

    override fun onPlayerLeave(player: ServerPlayerEntity) {
        val overlay = (player as IPlayerAccessor).serverutils_showHat()
        val avatarUrl = createAvatarUrl(player.gameProfile, overlay)
        val webhookMessage = WebhookMessageBuilder()
            .setAllowedMentions(allowedMentions)
            .setUsername("Server")
            .setAvatarUrl(this.config[DiscordBotSpec.serverIcon])
            .addEmbeds(WebhookEmbedBuilder()
                .setDescription("")
                .setAuthor(WebhookEmbed.EmbedAuthor("${player.gameProfile.name} left the game!", avatarUrl, ""))
                .setColor(0xFF0000)
                .build())
            .build()
        this.eventWebhookClient?.send(webhookMessage)
    }
}
