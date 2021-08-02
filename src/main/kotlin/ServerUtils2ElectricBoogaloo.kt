package me.geek.tom.serverutils

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import com.uchuhimo.konf.Config
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.geek.tom.serverutils.bot.BotConnection
import me.geek.tom.serverutils.bot.loadBot
import me.geek.tom.serverutils.commands.registerBroadcastCommand
import me.geek.tom.serverutils.commands.registerHomesCommand
import me.geek.tom.serverutils.crashreports.CrashReportHelper
import me.geek.tom.serverutils.ducks.IPlayerAccessor
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.filter.TextStream
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.crash.CrashReport
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

class ServerUtils2ElectricBoogaloo : ModInitializer {
    override fun onInitialize() {
        LOGGER.info("Initializing TomsServerUtils...")
        config = loadConfig(FabricLoader.getInstance().configDir)
        homesConfig = HomesConfig(config!!)
        connection = loadBot(config!!)
        crashHelper = loadCrashHelper(config!!)

        // Only register the test crash commands in development. It should be obvious why we do this.
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, dedicated: Boolean ->
                if (dedicated) {
                    dispatcher.register(
                        CommandManager.literal("testcrash")
                            .requires { s: ServerCommandSource ->
                                s.hasPermissionLevel(
                                    4
                                )
                            }
                            .then(
                                CommandManager.argument("save_report", BoolArgumentType.bool())
                                    .executes { ctx: CommandContext<ServerCommandSource> ->
                                        ctx.source.sendFeedback(LiteralText("cya later!"), false)
                                        debugCommandSaveReport = BoolArgumentType.getBool(ctx, "save_report")
                                        throw Error("Debug crash!")
                                    }
                            )
                    )
                }
            })
        }

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _ ->
            if (homesConfig!!.enabled) {
                registerHomesCommand(dispatcher)
            }
            registerBroadcastCommand(dispatcher)
        })
    }
}

val LOGGER: Logger = LogManager.getLogger()

const val MOD_ID = "toms-server-utils"
@Suppress("unused")
const val MOD_NAME = "TomsServerUtils"

/**
 * See [net.minecraft.client.render.entity.PlayerModelPart]
 */
const val HAT_DISPLAY_MASK = 1 shl 6

var config: Config? = null

private var connection: BotConnection? = null
private var crashHelper: CrashReportHelper? = null
var homesConfig: HomesConfig? = null


var debugCommandSaveReport = true

fun broadcast(text: Text) {
    connection!!.onBroadcast(text)
}

fun crashed(report: CrashReport, saved: Boolean, file: File) {
    try {
        crashHelper?.handleCrashReport(
            report, saved && (!FabricLoader.getInstance().isDevelopmentEnvironment || debugCommandSaveReport),  // allow spoof a failed save for testing.
            file)
    } catch (e: Exception) {
        LOGGER.error("Failed to send crash report to Discord!", e)
    }
}

fun starting(server: MinecraftServer) {
    GlobalScope.launch {
        connection?.connect(server)
        connection?.serverStarting(server) // this is inside the GlobalScope.launch block to ensure it is called after we have connected
    }
}

fun started(server: MinecraftServer) {
    connection?.serverStarted(server)
}

fun stopping(server: MinecraftServer) {
    connection?.serverStopping(server)
}

fun stopped(server: MinecraftServer) {
    connection?.serverStopped(server)
    runBlocking {
        connection?.disconnect()
    }
}

fun onPlayerAnnouncement(player: ServerPlayerEntity, text: Text, colour: Int) {
    connection?.onPlayerAnnouncement(player, text, colour)
}

fun join(player: ServerPlayerEntity) {
    connection?.onPlayerJoin(player)
}

fun leave(player: ServerPlayerEntity) {
    connection?.onPlayerLeave(player)
}

fun chat(netHandler: ServerPlayNetworkHandler, message: TextStream.Message) {
    val player = netHandler.player
    val showHat = (player as IPlayerAccessor).serverutils_showHat()
    connection?.onChatMessage(player.gameProfile, showHat, message.filtered)
}
