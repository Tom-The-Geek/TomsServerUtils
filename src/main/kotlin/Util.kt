package me.geek.tom.serverutils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent

fun Component.clickToCopy(tooltip: String, toCopy: String): Component {
    return this.hoverEvent(HoverEvent.showText(Component.text(tooltip)))
        .clickEvent(ClickEvent.copyToClipboard(toCopy))
}
