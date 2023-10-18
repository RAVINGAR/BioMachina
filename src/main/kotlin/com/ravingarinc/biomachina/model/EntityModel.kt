package com.ravingarinc.biomachina.model

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.api.Version
import com.ravingarinc.biomachina.api.toDegreeBytes
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

interface EntityModel : Model {
    var entity: Entity?

    override fun create(x: Double, y: Double, z: Double, world: World) {
        entity = spawn(x, y, z, world)
        (parent as? EntityModel)?.let { p ->
            this.consumeEach { if(it is EntityModel) p.addPassenger(it) }
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

    fun show(version: Version) : PacketContainer? {
        entity?.let {
            val loc = it.location
            return version.spawnEntity(it.entityId, it.uniqueId, it.type, loc.x, loc.y, loc.z, loc.pitch.toDegreeBytes(), loc.yaw.toDegreeBytes(), 0)
        }
        return null
    }

    fun hide(version: Version) : PacketContainer? {
        entity?.let {
            val packet = Version.protocol.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
            packet.intLists.write(0, listOf(it.entityId))
            return packet
        }
        return null
    }
}