package com.ravingarinc.biomachina.animation

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.api.Version
import com.ravingarinc.api.Versions
import com.ravingarinc.biomachina.model.VehicleModel
import com.ravingarinc.biomachina.protocol.PacketHandler
import com.ravingarinc.biomachina.viewer.Group
import com.ravingarinc.biomachina.viewer.MutableViewGroup
import com.ravingarinc.biomachina.viewer.Viewer
import java.util.concurrent.atomic.AtomicBoolean

class AnimationController<T : VehicleModel>(handler: AnimationHandler, val model: T) {
    private val isDisposed = AtomicBoolean(false)
    private val viewers: MutableViewGroup = Group(model.getEntityUUID())
    private val hidingViewers: MutableViewGroup = Group()
    private val entityId: Int = model.getEntityId()
    private val animations: MutableList<Animation<T>> = ArrayList()

    val version: Version = Versions.version
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
        model.update(this)
    }

    fun addViewer(viewer: Viewer) {
        if(!hidingViewers.contains(viewer)) {
            this.viewers.add(viewer)
        }

    }

    fun removeViewer(viewer: Viewer) {
        if(hidingViewers.contains(viewer)) {
            this.viewers.remove(viewer)
        }
    }

    fun show(viewer: Viewer) {
        if(hidingViewers.remove(viewer) && this.viewers.add(viewer)) {

        }
    }

    fun hide(viewer: Viewer) {
        if(hidingViewers.add(viewer) && viewers.remove(viewer)) {

        }
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