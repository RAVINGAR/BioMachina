package com.ravingarinc.biomachina.animation

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.biomachina.api.Version
import com.ravingarinc.biomachina.api.Versions
import com.ravingarinc.biomachina.model.api.EntityModel
import com.ravingarinc.biomachina.protocol.PacketHandler
import com.ravingarinc.biomachina.viewer.Group
import com.ravingarinc.biomachina.viewer.Viewer
import java.util.concurrent.atomic.AtomicBoolean

class AnimationController<T : EntityModel>(handler: AnimationHandler, val model: T, private val applier: AnimationController<T>.(T) -> Unit) {
    private val isDisposed = AtomicBoolean(false)
    private val viewers: Group = Group(model.getEntityUUID())
    private val entityId: Int = model.getEntityId()
    private val animations: MutableList<Animation<T>> = ArrayList()

    val version: Version = Versions.serverVersion
    init {
        handler.addExemption(entityId, this)
    }

    fun animate(animation: Animation<T>) {
        animations.add(animation)
    }

    fun tick() {
        if(viewers.size() == 0) return

        for(i in animations.indices.reversed()) {
            if(animations[i].animate(this)) {
                animations.removeAt(i)
            }
        }
        applier.invoke(this, model)
    }

    fun addViewer(viewer: Viewer) {
        this.viewers.addViewer(viewer)
    }

    fun removeViewer(viewer: Viewer) {
        this.viewers.removeViewer(viewer)
    }

    fun createPacket(type: PacketType, modifier: (PacketContainer) -> Unit) : PacketContainer {
        return PacketHandler.create(type, modifier)
    }

    fun sendPacket(packets: Array<PacketContainer>) {
        viewers.sendPacket(*packets)
    }

    fun dispose(handler: AnimationHandler) {
        isDisposed.set(true)
        handler.removeExemption(entityId)
        viewers.destroy()
    }

    fun isDisposed() : Boolean {
        return isDisposed.get()
    }
}