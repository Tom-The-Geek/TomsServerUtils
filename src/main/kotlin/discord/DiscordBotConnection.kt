package me.geek.tom.serverutils.discord

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.mojang.authlib.GameProfile
import com.uchuhimo.konf.Config
import dev.kord.core.Kord
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializerOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import me.geek.tom.serverutils.DiscordBotSpec
import me.geek.tom.serverutils.MiscSpec
import me.geek.tom.serverutils.bot.BotConnection
import me.geek.tom.serverutils.discord.extensions.MinecraftExtension
import me.geek.tom.serverutils.ducks.IPlayerAccessor
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import okhttp3.OkHttpClient
import okhttp3.Protocol
import org.koin.dsl.module

class DiscordBotConnection(private val config: Config) : BotConnection {

    private lateinit var jda: ExtensibleBot
    private var chatWebhookClient: WebhookClient? = null
    private var eventWebhookClient: WebhookClient? = null
    private lateinit var server: MinecraftServer
    private var minecraftSerializer: MinecraftSerializer? = null
    private val allowedMentions by lazy {
        AllowedMentions.none()
            .withParseEveryone(this.config[DiscordBotSpec.AllowedMentions.everyone])
            .withParseUsers(this.config[DiscordBotSpec.AllowedMentions.users])
            .withParseRoles(this.config[DiscordBotSpec.AllowedMentions.roles])
    }

    override suspend fun connect(server: MinecraftServer) {
        this.jda = ExtensibleBot(this.config[DiscordBotSpec.token]) {
            if (config[DiscordBotSpec.presenceEnabled]) {
                presence {
                    watching("over the server!")
                }

            }

            messageCommands {
                prefix { "/" }
            }

            extensions {
                add(::MinecraftExtension)
            }

            module { single { server } }
        }

        @Suppress("DeferredResultUnused")
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.async {
            jda.start()
        }

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
            .addRenderer(MentionToMinecraftRenderer(jda)))
    }

    override suspend fun disconnect() {
        val kord = jda.getKoin().get<Kord>()
        kord.logout()
        kord.cancel()
        if (minecraftSerializer != null) {
            minecraftSerializer = null
        }
        chatWebhookClient?.close()
        eventWebhookClient?.close()
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

    override fun onBroadcast(text: Text) {
        val component = FabricServerAudiences.of(this.server).toAdventure(text)
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
