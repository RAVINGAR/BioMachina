package com.ravingarinc.biomachina.api.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player

fun TextComponent.Builder.callback(hoverValue: String, onClick: (Player) -> Unit) : TextComponent.Builder {
    this.hoverEvent(HoverEvent.showText(Component.text(hoverValue)))
        .clickEvent(ClickEvent.callback({
            if (it is Player) onClick.invoke(it)
        }, { builder ->
            builder.lifetime(ClickCallback.DEFAULT_LIFETIME)
            builder.uses(ClickCallback.UNLIMITED_USES)
        }))
    return this
}