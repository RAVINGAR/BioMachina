package com.ravingarinc.biomachina.viewer

import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.biomachina.protocol.PacketHandler
import com.ravingarinc.biomachina.protocol.PacketHandler.Companion.send
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

class Individual(private val player: Player) : Viewer {
    override val uniqueId: UUID = player.uniqueId
    private val parents: MutableSet<ViewGroup> = HashSet()
    override fun addParent(parent: ViewGroup) {
        parents.add(parent)
    }

    override fun removeParent(parent: ViewGroup) {
        parents.remove(parent)
    }

    override fun destroy() {
        parents.forEach { parent ->
            if(parent is MutableViewGroup) {
                parent.removeViewer(this)
            }
        }
    }

    override fun sendPacket(vararg packet: PacketContainer) {
        packet.forEach { it.send(player) }
    }

    override fun contains(viewer: Viewer): Boolean {
        return viewer.uniqueId == uniqueId
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun consume(action: (Viewer) -> Unit) {
        action.invoke(this)
    }

    override fun equals(other: Any?): Boolean {
        if(other is Individual) {
            return other.uniqueId == uniqueId
        }
        return false
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }
}