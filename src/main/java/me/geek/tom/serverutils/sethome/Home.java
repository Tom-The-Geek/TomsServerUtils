package me.geek.tom.serverutils.sethome;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Home {

    private static final DynamicCommandExceptionType WORLD_NOT_FOUND = new DynamicCommandExceptionType(world -> new LiteralText("The world: " + world + " no longer exists to teleport to!"));

    @NotNull private final String name;
    @NotNull private final RegistryKey<World> dimension;
    @NotNull private final BlockPos pos;

    public Home(@NotNull String name, @NotNull RegistryKey<World> dimension, @NotNull BlockPos pos) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
    }

    @NotNull
    public Text toMessage(ServerPlayerEntity player) {
        boolean canTeleport = canTeleport(player);
        Formatting colour = canTeleport ? Formatting.GREEN : Formatting.RED;
        MutableText text = new LiteralText("-=[ " + this.getName() + " ]=-").formatted(colour);
        if (canTeleport) text.styled(s -> s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/home tp " + this.getName())));
        return text;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public RegistryKey<World> getDimension() {
        return dimension;
    }

    @NotNull
    public BlockPos getPos() {
        return pos;
    }

    @NotNull
    public CompoundTag write(CompoundTag tag) {
        tag.putString("Name", this.getName());
        tag.putString("Dim", this.getDimension().getValue().toString());
        tag.put("Pos", NbtHelper.fromBlockPos(this.getPos()));
        return tag;
    }

    public static Home read(CompoundTag tag) {
        String name = tag.getString("Name");
        RegistryKey<World> dimension = RegistryKey.of(Registry.DIMENSION, new Identifier(tag.getString("Dim")));
        BlockPos pos = NbtHelper.toBlockPos(tag.getCompound("Pos"));
        return new Home(name, dimension, pos);
    }

    public boolean canTeleport(ServerPlayerEntity player) {
        boolean inSameWorld = player.getServerWorld().getRegistryKey().getValue().equals(this.getDimension().getValue());
        return TomsServerUtils.homesConfig.getAllowCrossDimension() || inSameWorld;
    }

    public void teleport(ServerPlayerEntity player) throws CommandSyntaxException {
        ServerWorld world = Objects.requireNonNull(player.getServer(), "player.getServer()").getWorld(this.getDimension());
        if (world == null) throw WORLD_NOT_FOUND.create(this.getDimension().getValue());
        BlockPos pos = this.getPos();
        player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
    }
}
