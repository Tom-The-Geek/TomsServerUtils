package me.geek.tom.serverutils;

import com.uchuhimo.konf.Config;
import me.geek.tom.serverutils.bot.BotConnection;
import me.geek.tom.serverutils.ducks.IPlayerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.StringUtils;
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

        ServerLifecycleEvents.SERVER_STARTED.register(connection::connect);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> connection.disconnect());
    }

    public static void join(ServerPlayerEntity player) {
        connection.onPlayerJoin(player);
    }

    public static void leave(ServerPlayerEntity player) {
        connection.onPlayerLeave(player);
    }

    public static void chat(ServerPlayNetworkHandler netHandler, ChatMessageC2SPacket packet) {
        String message = StringUtils.normalizeSpace(packet.getChatMessage());
        ServerPlayerEntity player = netHandler.player;
        boolean showHat = ((IPlayerAccessor) player).serverutils_showHat();
        connection.onChatMessage(player.getGameProfile(), showHat, message);
    }
}