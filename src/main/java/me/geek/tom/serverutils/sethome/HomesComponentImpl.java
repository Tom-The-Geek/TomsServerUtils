package me.geek.tom.serverutils.sethome;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.geek.tom.serverutils.TomsServerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomesComponentImpl implements HomesComponent {

    private static final DynamicCommandExceptionType HOME_EXISTS = new DynamicCommandExceptionType(name -> new LiteralText("You already have a home called: " + name));
    public static final DynamicCommandExceptionType HOME_NOT_FOUND = new DynamicCommandExceptionType(name -> new LiteralText("You do not have a home called: " + name));
    private static final DynamicCommandExceptionType TOO_MANY_HOMES = new DynamicCommandExceptionType(max -> new LiteralText("Could not create a new home, you are at the limit of " + max + " homes!"));

    private final List<Home> homes = new ArrayList<>();
    private final Object2ObjectOpenHashMap<String, Home> homesByName = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Identifier, List<Home>> homesByWorld = new Object2ObjectOpenHashMap<>();

    // Used as we need to implement EntityComponentFactory<PlayerComponent<? extends HomesComponent>, PlayerEntity>
    // with a constructor.
    @SuppressWarnings({"RedundantSuppression", "unused"})
    public HomesComponentImpl(PlayerEntity player) { }

    @Override
    public @Nullable Home getByName(@NotNull String name) {
        return this.homesByName.get(name);
    }

    @Override
    public @NotNull List<Home> getAllInDimension(@NotNull RegistryKey<World> dimension) {
        return Collections.unmodifiableList(this.homesByWorld.computeIfAbsent(dimension.getValue(), __ -> new ArrayList<>()));
    }

    @Override
    public @NotNull List<Home> getAllHomes() {
        return Collections.unmodifiableList(this.homes);
    }

    @Override
    public @NotNull Home createNewHome(@NotNull String name, @NotNull RegistryKey<World> dimension, @NotNull BlockPos pos) throws CommandSyntaxException {
        if (this.homesByName.containsKey(name)) throw HOME_EXISTS.create(name);
        int maxHomes = TomsServerUtils.homesConfig.getMaxHomeAmount();
        if (maxHomes != -1 && this.homes.size() == maxHomes) throw TOO_MANY_HOMES.create(maxHomes);

        Home home = new Home(name, dimension, pos);
        this.homes.add(home);
        // Update maps for fast lookup.
        this.homesByName.put(home.getName(), home);
        this.homesByWorld.computeIfAbsent(home.getDimension().getValue(), __ -> new ArrayList<>()).add(home);
        return home;
    }

    @Override
    public void removeHome(@NotNull Home home) {
        this.homesByName.remove(home.getName());
        this.homesByWorld.remove(home.getDimension().getValue());
        this.homes.remove(home);
    }

    @Override
    public void removeHomeByName(@NotNull String name) throws CommandSyntaxException {
        if (!this.homesByName.containsKey(name)) {
            throw HOME_NOT_FOUND.create(name);
        }
        this.removeHome(this.homesByName.get(name));
    }

    @Override
    public void removeHomesInDimension(@NotNull RegistryKey<World> dimension) {
        this.homesByWorld.computeIfAbsent(dimension.getValue(), __ -> new ArrayList<>()).forEach(this::removeHome);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag) {
        this.homes.clear();
        this.homesByWorld.clear();
        this.homesByName.clear();

        ListTag homes = tag.getList("Homes", 10);
        homes.stream().map(t -> (CompoundTag)t).map(Home::read).forEach(this.homes::add);

        // Compute the maps for easier lookup
        this.homes.forEach(h -> this.homesByName.put(h.getName(), h));
        this.homes.forEach(h -> this.homesByWorld.computeIfAbsent(h.getDimension().getValue(), __ -> new ArrayList<>()).add(h));
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        ListTag homes = new ListTag();
        this.homes.stream().map(home -> home.write(new CompoundTag())).forEach(homes::add);
        tag.put("Homes", homes);
    }
}
