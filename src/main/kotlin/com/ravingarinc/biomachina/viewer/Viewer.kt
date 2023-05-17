package com.ravingarinc.biomachina.viewer

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.Location
import org.bukkit.Sound
import java.util.*
import java.util.function.Consumer
import java.util.function.Function

interface Viewer {
    /**
     * If this Viewer is added to any other Viewer, it should be registered via this addParent.
     *
     * @param parent
     */
    fun addParent(parent: ViewGroup)

    /**
     * If this Viewer is removed from an existing parent, then remove it from the parent
     *
     * @param parent
     */
    fun removeParent(parent: ViewGroup)

    /**
     * Called when a viewer should be destroyed and all references should be removed
     */
    fun destroy()
    fun sendPacket(vararg packet: PacketContainer)
    operator fun contains(viewer: Viewer): Boolean
    fun sendMessage(message: String)
    fun consume(action: (Viewer) -> Unit)

    val uniqueId: UUID
}
