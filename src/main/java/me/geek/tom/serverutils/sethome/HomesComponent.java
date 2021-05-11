package me.geek.tom.serverutils.sethome;

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import me.geek.tom.serverutils.homes.Home;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@SuppressWarnings("UnstableApiUsage")
public interface HomesComponent extends PlayerComponent<HomesComponent> {
    @Nullable Home getByName(@NotNull String name);
    @NotNull List<Home> getAllInDimension(@NotNull RegistryKey<World> dimension);
    @NotNull List<Home> getAllHomes();

    @NotNull Home createNewHome(@NotNull String name, @NotNull RegistryKey<World> dimension, @NotNull BlockPos pos);
    void removeHome(@NotNull Home home);
    @SuppressWarnings("unused")
    void removeHomeByName(@NotNull String name);
    @SuppressWarnings("unused")
    void removeHomesInDimension(@NotNull RegistryKey<World> dimension);
}
