package me.geek.tom.serverutils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent

fun Component.clickToCopy(tooltip: String, toCopy: String): Component {
    this.hoverEvent(HoverEvent.showText(Component.text(tooltip)))
    this.clickEvent(ClickEvent.copyToClipboard(toCopy))
    return this
}
