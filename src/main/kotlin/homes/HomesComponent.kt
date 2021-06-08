package me.geek.tom.serverutils.homes

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

@Suppress("UnstableApiUsage")
interface HomesComponent : PlayerComponent<HomesComponent> {
    fun getByName(name: String): Home?
    fun getAllInDimension(dimension: RegistryKey<World>): List<Home>
    val allHomes: List<Home>

    fun createNewHome(name: String, dimension: RegistryKey<World>, pos: BlockPos): Home
    fun removeHome(home: Home)
    fun removeHomeByName(name: String)
    fun removeHomesInDimension(dimension: RegistryKey<World>)
}
