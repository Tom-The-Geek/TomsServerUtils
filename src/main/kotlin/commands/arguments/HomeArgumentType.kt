package me.geek.tom.serverutils.commands.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.geek.tom.serverutils.Components
import me.geek.tom.serverutils.homes.Home
import me.geek.tom.serverutils.homes.HomesComponentImpl
import me.geek.tom.serverutils.homesConfig
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object HomeArgumentType {
    fun home(name: String): RequiredArgumentBuilder<ServerCommandSource, String> {
        return CommandManager.argument(name, StringArgumentType.string())
            .suggests { ctx: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder? ->
                val player = ctx.source.player
                val component = Components.HOMES.get(player)
                val showAll = homesConfig!!.allowCrossDimension
                val homes = if (showAll)
                    component.allHomes
                else
                    component.getAllInDimension(player.serverWorld.registryKey)

                val homeNames = homes.stream()
                    .map { obj: Home -> obj.name }

                CommandSource.suggestMatching(homeNames, builder)
            }
    }

    operator fun get(ctx: CommandContext<ServerCommandSource>, name: String): Home {
        val homeName = StringArgumentType.getString(ctx, name)
        val player = ctx.source.player
        val component = Components.HOMES.get(player)
        return component.getByName(homeName) ?: throw HomesComponentImpl.HOME_NOT_FOUND.create(homeName)
    }
}
