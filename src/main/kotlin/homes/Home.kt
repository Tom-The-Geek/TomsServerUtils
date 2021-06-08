package me.geek.tom.serverutils.homes

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import me.geek.tom.serverutils.homesConfig
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.ClickEvent
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.util.*

data class Home(
    val name: String,
    val dimension: RegistryKey<World>,
    val pos: BlockPos,
) {

    fun toMessage(player: ServerPlayerEntity): Text {
        val canTeleport = canTeleport(player)
        val colour = if (canTeleport) Formatting.GREEN else Formatting.RED
        val text = LiteralText("-=[ $name ]=-").formatted(colour)
        if (canTeleport) text.styled { s ->
            s.withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/home tp $name")) }
        return text
    }

    fun write(tag: NbtCompound): NbtCompound {
        tag.putString("Name", name)
        tag.putString("Dim", dimension.value.toString())
        tag.put("Pos", NbtHelper.fromBlockPos(pos))
        return tag
    }

    fun canTeleport(player: ServerPlayerEntity): Boolean {
        val inSameWorld = player.serverWorld.registryKey.value == dimension.value
        return homesConfig!!.allowCrossDimension || inSameWorld
    }

    fun teleport(player: ServerPlayerEntity) {
        val world = Objects.requireNonNull(player.getServer(), "player.getServer()")!!.getWorld(dimension)
            ?: throw WORLD_NOT_FOUND.create(dimension.value)
        val pos = pos
        player.teleport(world, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, 0f, 0f)
    }

    companion object {
        private val WORLD_NOT_FOUND = DynamicCommandExceptionType { world: Any? ->
            TranslatableText(
                "serverutils.home.tp.denied.worldnotexists",
                world
            )
        }

        fun read(tag: NbtCompound): Home {
            val name = tag.getString("Name")
            val dimension = RegistryKey.of(Registry.WORLD_KEY, Identifier(tag.getString("Dim")))
            val pos = NbtHelper.toBlockPos(tag.getCompound("Pos"))
            return Home(name, dimension, pos)
        }
    }
}
