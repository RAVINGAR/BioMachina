package com.ravingarinc.biomachina.animation

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.log
import com.ravingarinc.biomachina.model.api.EntityModel
import com.ravingarinc.biomachina.protocol.AnimationHandler
import com.ravingarinc.biomachina.protocol.PacketHandler
import com.ravingarinc.biomachina.viewer.Group
import com.ravingarinc.biomachina.viewer.Viewer
import kotlinx.coroutines.CoroutineScope
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

class AnimationController<T : EntityModel>(handler: AnimationHandler, private val model: T) {
    private val isDisposed = AtomicBoolean(false)
    private val viewers: Group = Group(model.getEntityUUID())
    private val internalIds: Set<Int> = buildSet {
        model.forEach {
            if(it is EntityModel) {
                val id = it.getEntityId()
                this.add(id)
                handler.addExemption(id, this@AnimationController)
            }
        }
    }
    private val animations: MutableList<Animation<T>> = ArrayList()

    fun animate(animation: Animation<T>) {
        animations.add(animation)
    }

    fun tick() {
        for(i in animations.indices.reversed()) {
            if(animations[i].animate(this)) {
                animations.removeAt(i)
            }
        }

    }

    fun addViewer(viewer: Viewer) {
        log(Level.INFO, "Adding viewer ${viewer.uniqueId}")
        this.viewers.addViewer(viewer)
    }

    fun removeViewer(viewer: Viewer) {
        log(Level.INFO, "Removing viewer ${viewer.uniqueId}")
        this.viewers.removeViewer(viewer)
    }

    fun createPacket(type: PacketType, modifier: (PacketContainer) -> Unit) : PacketContainer {
        return PacketHandler.create(type, modifier)
    }

    fun sendPacket(packets: List<PacketContainer>) {
        viewers.sendPacket(*packets.toTypedArray())
    }

    fun dispose(handler: AnimationHandler) {
        isDisposed.setRelease(true)
        internalIds.forEach {
            handler.removeExemption(it)
        }
        viewers.destroy()
    }

    fun isDisposed() : Boolean {
        return isDisposed.acquire
    }
}