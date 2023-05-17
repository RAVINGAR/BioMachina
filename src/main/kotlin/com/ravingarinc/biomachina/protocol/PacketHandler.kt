package com.ravingarinc.biomachina.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.api.module.severe
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException

interface PacketHandler {
    companion object {
        val PROTOCOL_MANAGER: ProtocolManager = ProtocolLibrary.getProtocolManager()

        fun create(type: PacketType, modifier: (PacketContainer) -> Unit) : PacketContainer {
            val packet = PROTOCOL_MANAGER.createPacket(type, true)
            modifier.invoke(packet)
            return packet
        }

        fun PacketContainer.send(player: Player) {
            try {
                PROTOCOL_MANAGER.sendServerPacket(player, this)
            } catch(exception: InvocationTargetException) {
                severe("Encountered exception sending packet to player!", exception)
            }
        }
    }




}