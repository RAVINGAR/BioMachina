package com.ravingarinc.biomachina.model

import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Boat
import org.bukkit.entity.Player

abstract class VehicleModel(val type: VehicleType) : RootModel(SpawnableModel(Boat::class.java, null) {
    it.setGravity(false)
    it.isInvulnerable = true
    it.isSilent = true
}) {

    abstract val chassis : CollidableModel

    /**
     * Reapply changes from model editor session. This should only be called under the circumstance of a session
     * since it is quite computationally expensive.
     */
    abstract fun apply(yaw: Float)

    abstract fun mountDriver(player: Player) : Boolean

    abstract fun mountPassenger(player: Player) : Boolean

    abstract fun update(controller: AnimationController<*>)
}
