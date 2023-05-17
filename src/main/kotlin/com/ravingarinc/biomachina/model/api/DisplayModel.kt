package com.ravingarinc.biomachina.model.api

import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.data.ModelVector
import com.ravingarinc.biomachina.data.copy
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

open class DisplayModel(val data: ModelData, parent: Model? = null) : VectorModel, SpawnableModel<ItemDisplay>(ItemDisplay::class.java, parent, {
    it.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED
    val item = ItemStack(Material.GUNPOWDER)
    val meta = item.itemMeta!!
    meta.setCustomModelData(data.customModelData)
    item.itemMeta = meta
    it.itemStack = item
}) {
    override val origin: Vector3f
        get() {
            (parent as? VectorModel)?.let { p ->
                return p.origin.add(data.origin)
            }
            return data.origin
        }
    override val leftRotation: Quaternionf
        get() {
            (parent as? VectorModel)?.let { p ->
                return p.leftRotation.mul(data.leftRotation)
            }
            return data.leftRotation
        }
    override val rightRotation: Quaternionf
        get() {
            (parent as? VectorModel)?.let { p ->
                return p.rightRotation.mul(data.rightRotation)
            }
            return data.rightRotation
        }
    override val scale: Vector3f
        get() {
            (parent as? VectorModel)?.let { p ->
                return p.scale.mul(data.scale)
            }
            return data.scale
        }
    override fun create(x: Double, y: Double, z: Double, yaw: Float, world: World) {
        super.create(x, y, z, yaw, world)
        castEntity?.let {
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.transformation = Transformation(origin, leftRotation, scale, rightRotation)
        }
    }

    override fun update() {
        super.update()
        castEntity?.let {
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.transformation = Transformation(origin, leftRotation, scale, rightRotation)
        }
    }
}