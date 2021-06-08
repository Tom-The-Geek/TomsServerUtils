package me.geek.tom.serverutils

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import me.geek.tom.serverutils.homes.HomesComponentImpl
import me.geek.tom.serverutils.homes.HomesComponent
import net.minecraft.util.Identifier

class Components : EntityComponentInitializer {
    companion object {
        val HOMES: ComponentKey<HomesComponent> = ComponentRegistry.getOrCreate(Identifier(MOD_ID, "homes"), HomesComponent::class.java)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerForPlayers(HOMES, ::HomesComponentImpl, RespawnCopyStrategy.ALWAYS_COPY)
    }
}
