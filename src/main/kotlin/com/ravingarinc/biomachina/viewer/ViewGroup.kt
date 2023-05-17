package com.ravingarinc.biomachina.viewer

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Location
import org.bukkit.Sound
import java.util.function.Function

interface ViewGroup : Viewer {
    override fun sendMessage(message: String) {
        consume { viewer: Viewer -> viewer.sendMessage(message) }
    }

    override fun sendPacket(vararg packet: PacketContainer) {
        consume { viewer: Viewer -> viewer.sendPacket(*packet) }
    }

    fun size(): Int
}
