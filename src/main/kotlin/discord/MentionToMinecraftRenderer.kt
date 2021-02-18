package me.geek.tom.serverutils.discord

import dev.vankka.mcdiscordreserializer.renderer.implementation.DefaultMinecraftRenderer
import me.geek.tom.serverutils.clickToCopy
import net.dv8tion.jda.api.JDA
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class MentionToMinecraftRenderer(
    private val jda: JDA,
) : DefaultMinecraftRenderer() {
    override fun appendChannelMention(component: Component, id: String): Component {
        return component.append(Component.text("#${jda.getGuildChannelById(id)?.name}")
            .color(BLURPLE)
            .clickToCopy("Click to copy mention", "<#$id>"))
    }

    override fun appendUserMention(component: Component, id: String): Component {
        return component.append(Component.text("@${jda.getUserById(id)?.name}")
            .color(BLURPLE)
            .clickToCopy("Click to copy mention", "<@!$id>"))
    }

    override fun appendRoleMention(component: Component, id: String): Component {
        return component.append(Component.text("@${jda.getRoleById(id)?.name}")
            .color(BLURPLE)
            .clickToCopy("Click to copy mention", "<@&$id>"))
    }

    companion object {
        private val BLURPLE = TextColor.fromHexString("#7289da")!!
    }
}
