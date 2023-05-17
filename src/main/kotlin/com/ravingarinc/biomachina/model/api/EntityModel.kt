package com.ravingarinc.biomachina.model.api

import com.ravingarinc.api.module.warn
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

interface EntityModel : Model {
    var entity: Entity?
    override fun create(x: Double, y: Double, z: Double, world: World) {
        entity = spawn(x, y, z, world)
        (parent as? EntityModel)?.let { p ->
            this.forEach {
                if(it is EntityModel) p.addPassenger(it)
            }
        }
    }

    fun getEntityId() : Int

    fun getEntityUUID() : UUID

    /**
     * Spawn the entity represented by this model
     */
    fun spawn(x: Double, y: Double, z: Double, world: World) : Entity?

    /**
     * Add the given entity model as a passenger of this model
     */
    fun addPassenger(model: EntityModel) {
        entity?.let { parent ->
            model.entity?.let { parent.addPassenger(it) }
        }
    }

    /**
     * Remove the given entity model as a passenger of this model
     */
    fun removePassenger(model: EntityModel) {
        entity?.let { parent ->
            model.entity?.let { parent.removePassenger(it) }
        }
    }

    fun getPassengerAmount() : Int {
        return entity?.passengers?.size ?: 0
    }
}