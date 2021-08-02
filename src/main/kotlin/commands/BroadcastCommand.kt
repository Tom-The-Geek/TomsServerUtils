package me.geek.tom.serverutils.commands

import com.github.p03w.aegis.aegisCommand
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import me.geek.tom.serverutils.broadcast
import net.minecraft.command.argument.TextArgumentType.getTextArgument
import net.minecraft.network.MessageType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Util

fun registerBroadcastCommand(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(aegisCommand("broadcast") {
        literal("raw") {
            text("message") {
                executes { ctx ->
                    val text = getTextArgument(ctx, "message")
                    broadcastMessage(ctx, text)
                }
            }
        }

        greedyString("message") {
            executes { ctx ->
                val message = getString(ctx, "message")
                val text = LiteralText(message)
                broadcastMessage(ctx, text)
            }
        }
    })
}

private fun broadcastMessage(ctx: CommandContext<ServerCommandSource>, text: Text) {
    val server = ctx.source.server
    server.playerManager.broadcastChatMessage(text, MessageType.CHAT, Util.NIL_UUID)
    broadcast(text)
}
