package com.ravingarinc.biomachina.model.api

import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.data.copy
import org.bukkit.World
import org.bukkit.entity.Entity
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import kotlin.properties.Delegates

open class RootModel(protected val root: EntityModel, parent: Model?) : EntityModel, ContainerModel(parent) {
    init {
        root.parent = parent
    }

    private var internalId by Delegates.notNull<Int>()
    private lateinit var internalUUID: UUID
    override var entity: Entity?
        get() = root.entity
        set(value) {
            root.entity = value
            internalId = value!!.entityId
            internalUUID = value.uniqueId
        }

    override fun getEntityId(): Int {
        return internalId
    }

    override fun getEntityUUID(): UUID {
        return internalUUID
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        super<ContainerModel>.create(x, y, z, world)
    }

    override fun spawn(x: Double, y: Double, z: Double, world: World): Entity? {
        throw IllegalStateException("Spawn should never be called on RootModel!")
    }


    override fun forEach(consumer: (Model) -> Unit) {
        consumer.invoke(root)
        super.forEach(consumer)
    }
}

