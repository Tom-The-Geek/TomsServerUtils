package me.geek.tom.serverutils.homes

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.geek.tom.serverutils.homesConfig
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.util.*
import java.util.function.Consumer

// Used as we need to implement EntityComponentFactory<PlayerComponent<? extends HomesComponent>, PlayerEntity>
// with a constructor.
class HomesComponentImpl(player: PlayerEntity) : HomesComponent {

    private val homes: MutableList<Home> = ArrayList()
    private val homesByName = Object2ObjectOpenHashMap<String, Home>()
    private val homesByWorld = Object2ObjectOpenHashMap<Identifier, MutableList<Home>>()

    override fun getByName(name: String): Home? {
        return homesByName[name]
    }

    override fun getAllInDimension(dimension: RegistryKey<World>): List<Home> {
        return Collections.unmodifiableList(homesByWorld.computeIfAbsent(dimension.value) { ArrayList() })
    }

    override val allHomes: List<Home> get() = Collections.unmodifiableList(this.homes)

    override fun createNewHome(name: String, dimension: RegistryKey<World>, pos: BlockPos): Home {
        if (homesByName.containsKey(name)) throw HOME_EXISTS.create(name)
        val maxHomes = homesConfig!!.maxHomeAmount
        if (maxHomes != -1) {
            if (homesConfig!!.maxHomesPerDimension) {
                if (homesByWorld.getOrDefault(dimension.value, emptyList()).size >= maxHomes) {
                    throw TOO_MANY_HOMES.create(maxHomes)
                }
            } else {
                if (homes.size >= maxHomes) {
                    throw TOO_MANY_HOMES.create(maxHomes)
                }
            }
        }
        val home = Home(name, dimension, pos)
        homes.add(home)
        // Update maps for fast lookup.
        homesByName[home.name] = home
        homesByWorld.computeIfAbsent(home.dimension.value) { ArrayList() }.add(home)
        return home
    }

    override fun removeHome(home: Home) {
        homesByName.remove(home.name)
        homesByWorld.remove(home.dimension.value)
        homes.remove(home)
    }

    override fun removeHomeByName(name: String) {
        if (!homesByName.containsKey(name)) {
            throw HOME_NOT_FOUND.create(name)
        }
        removeHome(homesByName[name]!!)
    }

    override fun removeHomesInDimension(dimension: RegistryKey<World>) {
        homesByWorld.computeIfAbsent(dimension.value) { ArrayList() }.forEach { home ->
            removeHome(home)
        }
    }

    override fun readFromNbt(tag: NbtCompound) {
        homes.clear()
        homesByWorld.clear()
        homesByName.clear()
        val homes = tag.getList("Homes", 10)
        homes.stream().map { t -> t as NbtCompound }.map(Home::read).forEach { home -> this.homes.add(home) }

        // Compute the maps for easier lookup
        this.homes.forEach(Consumer { h: Home ->
            homesByName[h.name] = h
        })
        this.homes.forEach(Consumer { h: Home ->
            homesByWorld.computeIfAbsent(h.dimension.value) { ArrayList() }.add(h)
        })
    }

    override fun writeToNbt(tag: NbtCompound) {
        val homes = NbtList()
        this.homes.stream().map { home: Home ->
            home.write(
                NbtCompound()
            )
        }.forEach { e: NbtCompound -> homes.add(e) }
        tag.put("Homes", homes)
    }

    companion object {
        private val HOME_EXISTS = DynamicCommandExceptionType { name ->
            TranslatableText("serverutils.home.create.failed.exists", name)
        }
        val HOME_NOT_FOUND = DynamicCommandExceptionType { name ->
            TranslatableText("serverutils.home.tp.denied.notexists", name)
        }
        private val TOO_MANY_HOMES = DynamicCommandExceptionType { max ->
            TranslatableText("serverutils.home.create.failed.limit", max)
        }
    }
}