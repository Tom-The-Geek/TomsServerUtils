package me.geek.tom.serverutils;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import me.geek.tom.serverutils.sethome.HomesComponent;
import me.geek.tom.serverutils.sethome.HomesComponentImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static me.geek.tom.serverutils.TomsServerUtils.MOD_ID;

public class Components implements EntityComponentInitializer {

    public static final ComponentKey<HomesComponent> HOMES = ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "homes"), HomesComponent.class);

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(HOMES, HomesComponentImpl::new);
    }
}
