package com.ravingarinc.biomachina.model.api

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import kotlin.properties.Delegates

open class SpawnableModel<T : Entity>(private val entityType: Class<T>,
                                      override var parent: Model?,
                                      private val consumer: Consumer<T>
) : EntityModel {
    override var entity: Entity?
        get() = innerEntity
        set(value) {
            innerEntity = value as? T
            internalId = innerEntity!!.entityId
            internalUUID = innerEntity!!.uniqueId
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
            consumer.accept(it)
        }
    }

    override fun destroy() {
        entity?.let {
            it.eject()
            it.remove()
        }
        entity = null
    }

    override fun update() {

    }

    override fun forEach(consumer: (Model) -> Unit) {
        consumer.invoke(this)
    }
}