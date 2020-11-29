package me.geek.tom.serverutils;

import com.uchuhimo.konf.Config;
import me.geek.tom.serverutils.bot.BotConnection;
import me.geek.tom.serverutils.ducks.IPlayerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static me.geek.tom.serverutils.ConfigKt.loadConfig;
import static me.geek.tom.serverutils.bot.BotConnectionKt.loadBot;

public class TomsServerUtils implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "toms-server-utils";
    public static final String MOD_NAME = "TomsServerUtils";

    /**
     * See {@link net.minecraft.client.render.entity.PlayerModelPart}
     */
    public static final int HAT_DISPLAY_MASK = 1 << 6;

    private static Config config;
    private static BotConnection connection;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        config = loadConfig(FabricLoader.getInstance().getConfigDir());
        connection = loadBot(config);
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