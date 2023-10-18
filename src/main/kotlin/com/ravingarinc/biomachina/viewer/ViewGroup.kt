package com.ravingarinc.biomachina.viewer

import com.comphenix.protocol.events.PacketContainer

interface ViewGroup : Viewer {
    override fun sendMessage(message: String) {
        apply { viewer: Viewer -> viewer.sendMessage(message) }
    }

    override fun sendPackets(packets: Iterable<PacketContainer>) {
        apply { viewer: Viewer -> viewer.sendPackets(packets) }
    }

    fun size(): Int
}
