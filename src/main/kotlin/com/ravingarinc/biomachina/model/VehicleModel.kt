package com.ravingarinc.biomachina.model

import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.data.CollisionBox
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Boat
import org.bukkit.entity.Player

abstract class VehicleModel(val type: VehicleType) : RootModel(SpawnableModel(Boat::class.java, null) {
    it.setGravity(false)
    it.isInvulnerable = true
    it.isSilent = true
}) {

    private var innerCollision: CollisionBox = initCollision()

    private fun initCollision() : CollisionBox {
        val box = type.collisionBox.model
        // Consider origin is 0,0,0. Scale of 1 is 1x1x1
        // If scale was simply 1, then this means, divde by 2
        val scaleX = box.scale.x / 2.0
        val scaleY = box.scale.y / 2.0
        val scaleZ = box.scale.z / 2.0

        val oX = box.origin.x
        val oY = box.origin.y
        val oZ = box.origin.z
        return CollisionBox(
            oX - scaleX, oY - scaleY, oZ - scaleZ,
            oX + scaleX, oY + scaleY, oZ + scaleZ
        )
    }
    val collisionBox: CollisionBox get() = innerCollision

    /**
     * Reapply changes from model editor session. This should only be called under the circumstance of a session
     * since it is quite computationally expensive.
     */
    open fun apply(yaw: Float) {
        innerCollision = initCollision()
    }

    abstract fun mountDriver(player: Player) : Boolean

    abstract fun mountPassenger(player: Player) : Boolean

    abstract fun update(controller: AnimationController<*>)
}