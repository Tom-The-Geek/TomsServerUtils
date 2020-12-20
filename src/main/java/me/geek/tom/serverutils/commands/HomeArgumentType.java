package me.geek.tom.serverutils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.geek.tom.serverutils.Components;
import me.geek.tom.serverutils.TomsServerUtils;
import me.geek.tom.serverutils.sethome.Home;
import me.geek.tom.serverutils.sethome.HomesComponent;
import me.geek.tom.serverutils.sethome.HomesComponentImpl;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.stream.Stream;

public class HomeArgumentType {
    public static RequiredArgumentBuilder<ServerCommandSource, String> home(String name) {
        return CommandManager.argument(name, StringArgumentType.string())
                .suggests((ctx, builder) -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    HomesComponent component = Components.HOMES.get(player);
                    boolean showAll = TomsServerUtils.homesConfig.getAllowCrossDimension();
                    Stream<String> homes = (showAll ? component.getAllHomes() : component.getAllInDimension(player.getServerWorld().getRegistryKey()))
                            .stream().map(Home::getName);

                    return CommandSource.suggestMatching(homes, builder);
                });
    }

    public static Home get(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        String homeName = StringArgumentType.getString(ctx, name);
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        HomesComponent component = Components.HOMES.get(player);
        Home home = component.getByName(homeName);
        if (home == null)
            throw HomesComponentImpl.HOME_NOT_FOUND.create(homeName);
        return home;
    }
}
