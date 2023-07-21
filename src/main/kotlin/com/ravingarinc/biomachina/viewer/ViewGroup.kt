package com.ravingarinc.biomachina.viewer

import com.comphenix.protocol.events.PacketContainer

interface ViewGroup : Viewer {
    override fun sendMessage(message: String) {
        apply { viewer: Viewer -> viewer.sendMessage(message) }
    }

    override fun sendPacket(vararg packet: PacketContainer) {
        apply { viewer: Viewer -> viewer.sendPacket(*packet) }
    }

    fun size(): Int
}
