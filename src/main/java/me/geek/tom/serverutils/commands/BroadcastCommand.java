package me.geek.tom.serverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.TextArgumentType.getTextArgument;
import static net.minecraft.command.argument.TextArgumentType.text;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("broadcast")
                .requires(s -> s.hasPermissionLevel(2))
                .then(literal("raw")
                        .then(argument("message", text())
                                .executes(ctx -> {
                                    Text text = getTextArgument(ctx, "message");
                                    broadcastMessage(ctx, text);
                                    return 0;
                                })
                        )
                ).then(argument("message", greedyString())
                        .executes(ctx -> {
                            String message = getString(ctx, "message");
                            Text text = new LiteralText(message);
                            broadcastMessage(ctx, text);
                            return 0;
                        })
                )
        );
    }

    private static void broadcastMessage(CommandContext<ServerCommandSource> ctx, Text text) {
        MinecraftServer server = ctx.getSource().getMinecraftServer();
        server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, Util.NIL_UUID);
        TomsServerUtils.broadcast(text);
    }
}
