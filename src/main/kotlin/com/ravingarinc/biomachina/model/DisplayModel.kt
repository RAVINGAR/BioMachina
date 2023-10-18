package com.ravingarinc.biomachina.model

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.ravingarinc.api.Version
import com.ravingarinc.api.build
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.data.CollisionBox
import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.data.ModelTransformation
import com.ravingarinc.biomachina.data.copy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f


abstract class DisplayModel<T : Display>(entityType: Class<T>, val data: ModelTransformation, parent: Model? = null, consumer: (T) -> Unit) : SpawnableModel<T>(entityType, parent, {
    consumer.invoke(it)
    //it.setRotation(180F, 0F)
}), CollidableModel {
    final override val origin: Vector3f = if(parent is VectorModel) parent.origin.copy().add(data.origin) else data.origin.copy()
    final override val leftRotation: Quaternionf = Quaternionf(0F, 0F, 0F, 1F)
    final override val rightRotation: Quaternionf = Quaternionf(0F, 0F, 0F, 1F)
    final override val scale: Vector3f = if(parent is VectorModel) parent.scale.copy().mul(data.scale) else data.scale.copy()

    override var inverted: Boolean = data.inverted
    override val rotatingOrigin: Vector3f = if(parent is VectorModel) parent.rotatingOrigin.copy() else Vector3f()

    final override var absYaw: Float = if(parent is VectorModel) parent.absYaw + data.yaw else data.yaw
    final override var absPitch: Float = if(parent is VectorModel) parent.absPitch + data.pitch else data.pitch
    final override var absRoll: Float = if(parent is VectorModel) parent.absRoll + data.roll else data.roll

    override var relYaw: Float = 0F
    override var relPitch: Float = 0F
    override var relRoll: Float = 0F

    abstract val metadata: MutableList<Triple<Int, WrappedDataWatcher.Serializer, Any>>


    private var innerCollision: CollisionBox? = null
    private var collisionVector: ModelTransformation? = null

    override val collisionBox: CollisionBox
        get() = innerCollision ?: throw IllegalStateException("Cannot get collision box on DisplayModel that does not have collision enabled!")

    override val isCollisionEnabled: Boolean get() = innerCollision != null

    init {
        applyAbsoluteRotations()
    }
    private fun applyAbsoluteRotations() {
        if(absPitch != 0F) {
            leftRotation.rotateLocalX(absPitch.toRadians())
        }
        if(absYaw != 0F) {
            leftRotation.rotateLocalY(absYaw.toRadians())
        }
        if(absRoll != 0F) {
            leftRotation.rotateLocalZ(absRoll.toRadians())
        }
    }

    fun enableCollision(model: ModelTransformation) {
        collisionVector = model
        val scaleX = model.scale.x
        val scaleY = model.scale.y
        val scaleZ = model.scale.z

        val oX = model.origin.x
        val oY = model.origin.y
        val oZ = model.origin.z
        innerCollision = CollisionBox(
            oX + scaleX, oY + scaleY, oZ + scaleZ,
            oX, oY, oZ
        )
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        super.create(x, y, z, world)
        castEntity?.let {
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.transformation = Transformation(origin, leftRotation, scale, rightRotation)
        }
    }

    override fun apply(yaw: Float, pitch: Float, roll: Float) {
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
            inverted = data.inverted
            leftRotation.set(Quaternionf(0F, 0F, 0F, 1F))
            applyAbsoluteRotations()
        }
        update() // We call update here to update the right rotation as well.
        rotatingOrigin.set(calculateRotationOffset(origin.x, origin.y, origin.z, yaw, pitch, roll))
        castEntity?.let {
            it.interpolationDelay = 0
            it.interpolationDuration = 0
            it.transformation = Transformation(origin.copy().add(rotatingOrigin), leftRotation, scale, rightRotation)
        }
        if(isCollisionEnabled) {
            enableCollision(collisionVector!!)
        }
    }

    override fun update() {
        rightRotation.set(0F, 0F, 0F, 1F)
        if (relPitch != 0F) {
            rightRotation.rotateLocalX(if (inverted) -relPitch else relPitch)
        }
        if (relYaw != 0F) {
            rightRotation.rotateLocalY(relYaw)
        }
        if (relRoll != 0F) {
            rightRotation.rotateLocalZ(relRoll)
        }
        metadata[2] = Triple(10, Version.V1_19_4.vectorSerializer, origin.copy().add(rotatingOrigin))
        metadata[5] = Triple(13, Version.V1_19_4.quaternionSerializer, rightRotation)
    }

    fun getAnimationPacket(controller: AnimationController<*>) : PacketContainer {
        return controller.version.updateMetadata(entity!!, metadata)
    }
}

open class ItemDisplayModel(data: ModelData, parent: Model? = null) : DisplayModel<ItemDisplay>(ItemDisplay::class.java, data, parent, {
    it.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED
}) {
    val customModelData: Int = data.data
    val item by lazy {
        val item = ItemStack(Material.GUNPOWDER)
        val meta = item.itemMeta!!
        meta.setCustomModelData(customModelData)
        item.itemMeta = meta
        return@lazy item
    }

    override val metadata: MutableList<Triple<Int, WrappedDataWatcher.Serializer, Any>>
        get() = metadataInner

    private val metadataInner : MutableList<Triple<Int, WrappedDataWatcher.Serializer, Any>> by lazy {
        val list = ArrayList<Triple<Int, WrappedDataWatcher.Serializer, Any>>()
        list.build(8, Version.integerSerializer, 0) // #0
        list.build(9, Version.integerSerializer, 2) // #1 Delay
        list.build(10, Version.V1_19_4.vectorSerializer, origin.copy().add(rotatingOrigin)) // #2
        list.build(11, Version.V1_19_4.vectorSerializer, scale) // #3
        list.build(12, Version.V1_19_4.quaternionSerializer, leftRotation) // #4
        list.build(13, Version.V1_19_4.quaternionSerializer, rightRotation) // #5


        // need to fill other defaults here!?
        list.build(22, Version.itemSerializer, MinecraftReflection.getMinecraftItemStack(item)) // #6
        list.build(23, Version.byteSerializer, java.lang.Byte.valueOf("8")) // #7
        return@lazy list
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        super.create(x, y, z, world)
        castEntity?.let {
            it.itemStack = item
        }
    }

    override fun show(version: Version): PacketContainer? {
        // Todo doesnt work
        entity?.let {
            val list = buildList {
                this.build(0, Version.byteSerializer, 0)
                metadata.forEach { triple ->
                    this.add(triple)
                }
            }
            return version.updateMetadata(it, list)
        }
        return null
    }

    override fun hide(version: Version): PacketContainer? {
        entity?.let {
            val list = ArrayList<Triple<Int, WrappedDataWatcher.Serializer, Any>>()
            list.build(0, Version.byteSerializer, (0x20))
            return version.updateMetadata(it, list)
        }

        return null
    }
}

open class BlockDisplayModel(data: ModelTransformation, material: Material, parent: Model? = null) : DisplayModel<BlockDisplay>(BlockDisplay::class.java, data, parent, {
    it.block = Bukkit.createBlockData(material)
}) {
    override val metadata: MutableList<Triple<Int, WrappedDataWatcher.Serializer, Any>>
        get() = metadataInner

    private val metadataInner: MutableList<Triple<Int, WrappedDataWatcher.Serializer, Any>> by lazy {
        val list = ArrayList<Triple<Int, WrappedDataWatcher.Serializer, Any>>()
        list.build(8, Version.integerSerializer, 0) // #0
        list.build(9, Version.integerSerializer, 1) // #1
        list.build(10, Version.V1_19_4.vectorSerializer, origin.copy().add(rotatingOrigin)) // #2
        list.build(11, Version.V1_19_4.vectorSerializer, scale) // #3
        list.build(12, Version.V1_19_4.quaternionSerializer, leftRotation) // #4
        list.build(13, Version.V1_19_4.quaternionSerializer, rightRotation) // #5

        list.build(22, Version.integerSerializer, 94) // TODO, Need to lookup block state IDs
        return@lazy list
    }
}