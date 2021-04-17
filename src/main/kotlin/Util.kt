package me.geek.tom.serverutils

import dev.kord.common.Color
import dev.kord.core.entity.Member
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent

fun Component.clickToCopy(tooltip: String, toCopy: String): Component {
    return this.hoverEvent(HoverEvent.showText(Component.text(tooltip)))
        .clickEvent(ClickEvent.copyToClipboard(toCopy))
}

private val defaultRoleColour = Color(0x9ea9b4)

suspend fun Member.colour(): Int? {
    for (role in roles.toList()) {
        if (role.color.rgb != 0) {
            return role.color.rgb
        }
    }
    return null
}
