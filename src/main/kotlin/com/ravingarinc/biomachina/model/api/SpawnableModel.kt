package com.ravingarinc.biomachina.model.api

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*
import kotlin.properties.Delegates

open class SpawnableModel<T : Entity>(private val entityType: Class<T>,
                                      override val parent: Model?,
                                      private val consumer: (T) -> Unit
) : EntityModel {
    override var entity: Entity?
        get() = innerEntity
        set(value) {
            innerEntity = value as? T
            value?.let {
                internalId = value.entityId
                internalUUID = value.uniqueId
            }

        }
    protected var innerEntity: T? = null

    private var internalId by Delegates.notNull<Int>()
    private lateinit var internalUUID: UUID

    override fun getEntityId(): Int {
        return internalId
    }

    override fun getEntityUUID(): UUID {
        return internalUUID
    }

    val castEntity: T?
        get() = innerEntity

    override fun spawn(x: Double, y: Double, z: Double, world: World) : Entity? {
        return world.spawn(Location(world, x, y, z), entityType) {
            consumer.invoke(it)
        }
    }

    override fun destroy() {
        entity?.let {
            it.eject()
            it.remove()
        }
        entity = null
    }

    override fun forEach(consumer: (Model) -> Unit) {
        consumer.invoke(this)
    }
}