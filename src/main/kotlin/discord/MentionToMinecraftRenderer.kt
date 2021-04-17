package me.geek.tom.serverutils.discord

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.cache.api.getEntry
import dev.kord.cache.api.query
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.vankka.mcdiscordreserializer.renderer.implementation.DefaultMinecraftRenderer
import kotlinx.coroutines.runBlocking
import me.geek.tom.serverutils.clickToCopy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class MentionToMinecraftRenderer(
    private val bot: ExtensibleBot,
) : DefaultMinecraftRenderer() {
    override fun appendChannelMention(component: Component, id: String): Component {
        return runBlocking {
            component.append(
                Component.text("#${bot.kord.getChannel(Snowflake(id), EntitySupplyStrategy.cacheWithRestFallback)?.data?.name?.value}")
                    .color(BLURPLE)
                    .clickToCopy("Click to copy mention", "<#$id>")
            )
        }
    }

    override fun appendUserMention(component: Component, id: String): Component {
        return runBlocking {
            component.append(
                Component.text("@${bot.kord.getUser(Snowflake(id))?.tag}")
                    .color(BLURPLE)
                    .clickToCopy("Click to copy mention", "<@!$id>")
            )
        }
    }

    override fun appendRoleMention(component: Component, id: String): Component {
        val snowflakeId = Snowflake(id)
        return runBlocking {
            component.append(
                Component.text(
                    "@${
                        // TODO: Improve by fetching the role from the guild to ensure that we get every valid role mention without relying on the cache
                        // Unless I find a better way, this is what we have to do
                        bot.kord.cache.getEntry<Role>()?.query { Role::id eq snowflakeId }?.singleOrNull()?.name
                    }").color(BLURPLE)
                    .clickToCopy("Click to copy mention", "<@&$snowflakeId>")
            )
        }
    }

    companion object {
        private val BLURPLE = TextColor.fromHexString("#7289da")!!
    }
}
