package me.geek.tom.serverutils;

import com.uchuhimo.konf.Config;
import me.geek.tom.serverutils.bot.BotConnection;
import me.geek.tom.serverutils.commands.HomeCommand;
import me.geek.tom.serverutils.crashreports.CrashReportHelper;
import me.geek.tom.serverutils.ducks.IPlayerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.crash.CrashReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static me.geek.tom.serverutils.ConfigKt.loadConfig;
import static me.geek.tom.serverutils.ConfigKt.loadCrashHelper;
import static me.geek.tom.serverutils.bot.BotConnectionKt.loadBot;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TomsServerUtils implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "toms-server-utils";
    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static final String MOD_NAME = "TomsServerUtils";

    /**
     * See {@link net.minecraft.client.render.entity.PlayerModelPart}
     */
    public static final int HAT_DISPLAY_MASK = 1 << 6;

    private static BotConnection connection;
    private static CrashReportHelper crashHelper;
    public static HomesConfig homesConfig;

    public static boolean debugCommandSaveReport = true;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        Config config = loadConfig(FabricLoader.getInstance().getConfigDir());
        homesConfig = new HomesConfig(config);
        connection = loadBot(config);
        crashHelper = loadCrashHelper(config);

        // Only register the test crash commands in development. It should be obvious why we do this.
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                if (dedicated) {
                    dispatcher.register(
                            literal("testcrash")
                                    .requires(s -> s.hasPermissionLevel(4))
                                    .then(argument("save_report", bool()).executes(ctx -> {
                                        ctx.getSource().sendFeedback(new LiteralText("cya later!"), false);
                                        debugCommandSaveReport = getBool(ctx, "save_report");
                                        throw new Error("Debug crash!");
                                    }))
                    );
                }
            });
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, __) -> {
            if (homesConfig.getEnabled()) {
                HomeCommand.register(dispatcher);
            }
        });
    }

    public static void crashed(CrashReport report, boolean saved, File file) {
        crashHelper.handleCrashReport(report, saved &&
                        (!FabricLoader.getInstance().isDevelopmentEnvironment() || debugCommandSaveReport), // allow spoof a failed save for testing.
                file);
    }

    public static void starting(MinecraftServer server) {
        connection.connect(server);
        connection.serverStarting(server);
    }

    public static void started(MinecraftServer server) {
        connection.serverStarted(server);
    }

    public static void stopping(MinecraftServer server) {
        connection.serverStopping(server);
    }

    public static void stopped(MinecraftServer server) {
        connection.serverStopped(server);
        connection.disconnect();
    }

    public static void join(ServerPlayerEntity player) {
        connection.onPlayerJoin(player);
    }

    public static void leave(ServerPlayerEntity player) {
        connection.onPlayerLeave(player);
    }

    public static void chat(ServerPlayNetworkHandler netHandler, String message) {
        ServerPlayerEntity player = netHandler.player;
        boolean showHat = ((IPlayerAccessor) player).serverutils_showHat();
        connection.onChatMessage(player.getGameProfile(), showHat, message);
    }
}