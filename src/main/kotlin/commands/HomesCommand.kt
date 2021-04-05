package me.geek.tom.serverutils.commands

import com.github.p03w.aegis.aegisCommand
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import me.geek.tom.serverutils.Components
import me.geek.tom.serverutils.commands.arguments.HomeArgumentType
import me.geek.tom.serverutils.commands.arguments.HomeArgumentType.home
import me.geek.tom.serverutils.homesConfig
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

private val NOT_ALLOWED_CROSS_DIM =
    SimpleCommandExceptionType(TranslatableText("serverutils.home.tp.denied.cross-dimension"))

fun registerHomesCommand(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(aegisCommand("home") {
        literal("list") {
            executes { ctx -> listHomes(ctx, false) }

            literal("all") {
                executes { ctx -> listHomes(ctx, true) }
            }
        }

        literal("set") {
            string("name") {
                executes { ctx -> createHome(ctx) }
            }
        }

        literal("tp") {
            custom(home("home")) {
                executes { ctx -> tpToHome(ctx) }
            }
        }

        literal("del") {
            custom(home("home")) {
                executes { ctx -> removeHome(ctx) }
            }
        }
    })
}

private fun removeHome(ctx: CommandContext<ServerCommandSource>): Int {
    val player = ctx.source.player
    val home = HomeArgumentType[ctx, "home"]
    val component = Components.HOMES.get(player)
    component.removeHome(home)
    ctx.source.sendFeedback(TranslatableText("serverutils.home.deleted", home.name), false)
    return Command.SINGLE_SUCCESS
}

private fun tpToHome(ctx: CommandContext<ServerCommandSource>): Int {
    val player = ctx.source.player
    val home = HomeArgumentType[ctx, "home"]
    if (!home.canTeleport(player)) throw NOT_ALLOWED_CROSS_DIM.create()
    home.teleport(player)
    ctx.source.sendFeedback(TranslatableText("serverutils.teleported", home.name), false)
    return Command.SINGLE_SUCCESS
}

private fun createHome(ctx: CommandContext<ServerCommandSource>): Int {
    val player = ctx.source.player
    val component = Components.HOMES.get(player)
    val home = component.createNewHome(
        StringArgumentType.getString(ctx, "name"),
        player.serverWorld.registryKey,
        player.blockPos
    )
    ctx.source.sendFeedback(TranslatableText("serverutils.home.created", home.name), false)
    return Command.SINGLE_SUCCESS
}

private fun listHomes(ctx: CommandContext<ServerCommandSource>, all: Boolean): Int {
    val player = ctx.source.player
    val component = Components.HOMES.get(player)
    val homes =
        if (all || homesConfig?.allowCrossDimension == true) component.allHomes else component.getAllInDimension(
            player.serverWorld.registryKey
        )
    val header =
        if (homes.isEmpty()) TranslatableText("serverutils.home.list.none").formatted(Formatting.RED) else TranslatableText(
            "serverutils.home.list"
        ).formatted(
            Formatting.GOLD
        )
    ctx.source.sendFeedback(header, false)
    homes.stream().map { h -> h.toMessage(player) }.forEach { text: Text? ->
        ctx.source.sendFeedback(text, false)
    }
    return Command.SINGLE_SUCCESS
}
