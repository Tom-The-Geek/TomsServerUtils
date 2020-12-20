package me.geek.tom.serverutils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.geek.tom.serverutils.Components;
import me.geek.tom.serverutils.TomsServerUtils;
import me.geek.tom.serverutils.sethome.Home;
import me.geek.tom.serverutils.sethome.HomesComponent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    private static final SimpleCommandExceptionType NOT_ALLOWED_CROSS_DIM = new SimpleCommandExceptionType(new LiteralText("You are not allowed to teleport to homes cross-dimensionally"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("home")
                .then(literal("list").executes(ctx -> listHomes(ctx, false))
                        .then(literal("all").executes(ctx -> listHomes(ctx, true))))
                .then(literal("set").then(argument("name", string()).executes(HomeCommand::createHome)))
                .then(literal("tp").then(HomeArgumentType.home("home").executes(HomeCommand::tpToHome)))
                .then(literal("del").then(HomeArgumentType.home("home").executes(HomeCommand::removeHome)))
        );
    }

    private static int removeHome(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Home home = HomeArgumentType.get(ctx, "home");
        HomesComponent component = Components.HOMES.get(player);
        component.removeHome(home);
        ctx.getSource().sendFeedback(new LiteralText("Deleted home: " + home.getName()), false);

        return 0;
    }

    private static int tpToHome(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Home home = HomeArgumentType.get(ctx, "home");
        if (!home.canTeleport(player)) throw NOT_ALLOWED_CROSS_DIM.create();
        home.teleport(player);
        ctx.getSource().sendFeedback(new LiteralText("Woosh!"), false);

        return 0;
    }

    private static int createHome(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        HomesComponent component = Components.HOMES.get(player);
        Home home = component.createNewHome(getString(ctx, "name"), player.getServerWorld().getRegistryKey(), player.getBlockPos());
        ctx.getSource().sendFeedback(new LiteralText("Created new home called " + home.getName() + "!"), false);

        return 0;
    }

    private static int listHomes(CommandContext<ServerCommandSource> ctx, boolean all) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        HomesComponent component = Components.HOMES.get(player);
        List<Home> homes = (all || TomsServerUtils.homesConfig.getAllowCrossDimension()) ? component.getAllHomes() :
                component.getAllInDimension(player.getServerWorld().getRegistryKey());

        MutableText header = homes.isEmpty() ?
                new LiteralText("You have no homes :(").formatted(Formatting.RED) :
                new LiteralText("Here is a list of your homes:").formatted(Formatting.GOLD);
        ctx.getSource().sendFeedback(header, false);
        homes.stream().map(h -> h.toMessage(player)).forEach(text -> ctx.getSource().sendFeedback(text, false));

        return 0;
    }
}
