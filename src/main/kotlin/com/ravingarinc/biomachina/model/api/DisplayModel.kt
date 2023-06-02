package com.ravingarinc.biomachina.model.api

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.data.copy
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

// Todo make it so the entity here is never null!
open class DisplayModel(val data: ModelData, parent: Model? = null) : SpawnableModel<ItemDisplay>(ItemDisplay::class.java, parent, {
    it.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED
    it.setRotation(180F, 0F)
}), VectorModel {
    val item by lazy {
        return@lazy constructItem(data)
    }
    private val dataWatcher by lazy {
        val watcher = WrappedDataWatcher()
        watcher.entity = entity ?: throw IllegalStateException("DisplayModel could not provide data watcher as entity does not exist yet!")
        return@lazy watcher
    }
    final override val origin: Vector3f = if(parent is VectorModel) parent.origin.copy().add(data.origin) else data.origin.copy()
    final override val leftRotation: Quaternionf = Quaternionf(0F, 0F, 0F, 1F)
    final override val rightRotation: Quaternionf = Quaternionf(0F, 0F, 0F, 1F)
    final override val scale: Vector3f = if(parent is VectorModel) parent.scale.copy().mul(data.scale) else data.scale.copy()

    override val rotatingOrigin: Vector3f = if(parent is VectorModel) parent.rotatingOrigin.copy() else Vector3f()

    final override var absYaw: Float = if(parent is VectorModel) parent.absYaw + data.yaw else data.yaw
    final override var absPitch: Float = if(parent is VectorModel) parent.absPitch + data.pitch else data.pitch
    final override var absRoll: Float = if(parent is VectorModel) parent.absRoll + data.roll else data.roll

    override var relYaw: Float = 0F
    override var relPitch: Float = 0F
    override var relRoll: Float = 0F

    init {
        if(absPitch != 0F) {
            leftRotation.rotateX(absPitch.toRadians())
        }
        if(absYaw != 0F) {
            leftRotation.rotateY(absYaw.toRadians())
        }
        if(absRoll != 0F) {
            leftRotation.rotateZ(absRoll.toRadians())
        }
    }

    override fun totalPitch(): Float {
        (parent as? VectorModel).let {
            return if(it == null) super.totalPitch() else (super.totalPitch() + it.relPitch) % 360F
        }
    }

    override fun totalYaw(): Float {
        (parent as? VectorModel).let {
            return if(it == null) super.totalYaw() else (super.totalYaw() + it.relYaw) % 360F
        }
    }

    override fun totalRoll(): Float {
        (parent as? VectorModel).let {
            return if(it == null) super.totalRoll() else (super.totalRoll() + it.relRoll) % 360F
        }
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        super.create(x, y, z, world)
        castEntity?.let {
            it.itemStack = item
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.transformation = Transformation(origin, leftRotation, scale, rightRotation)
        }
    }

    override fun reapply() {
        (parent as? VectorModel).let {
            if(it == null) {
                absYaw = data.yaw
                absPitch = data.pitch
                absRoll = data.roll

                origin.set(data.origin.copy())
                scale.set(data.scale.copy())
                //rightRotation.set(data.relativeRotation.copy())

            } else {
                absYaw = it.absYaw + data.yaw
                absPitch = it.absPitch + data.pitch
                absRoll = it.absRoll + data.roll
                origin.set(it.origin.copy().add(data.origin))
                //rightRotation.set(it.rightRotation.copy().mul(data.relativeRotation))
                scale.set(it.scale.copy().mul(data.scale))
            }
            leftRotation.set(Quaternionf(0F, 0F, 0F, 1F))
            if(absPitch != 0F) {
                leftRotation.rotateX(absPitch.toRadians())
            }
            if(absYaw != 0F) {
                leftRotation.rotateY(absYaw.toRadians())
            }
            if(absRoll != 0F) {
                leftRotation.rotateZ(absRoll.toRadians())
            }
        }
        castEntity?.let {
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.itemDisplayTransform
            it.transformation = Transformation(origin.copy().add(rotatingOrigin), leftRotation, scale, rightRotation)
        }
    }

    fun getTransformationPacket(controller: AnimationController<*>, delay: Int = 0, duration: Int = 0) : PacketContainer {
        return controller.version.transformDisplayEntity(dataWatcher, delay, duration, origin.copy().add(rotatingOrigin), scale, leftRotation, rightRotation, item)
    }

    companion object {
        fun constructItem(data: ModelData) : ItemStack {
            val item = ItemStack(Material.GUNPOWDER)
            val meta = item.itemMeta!!
            meta.setCustomModelData(data.data)
            item.itemMeta = meta
            return item
        }
    }
}