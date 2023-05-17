package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.model.api.EntityModel
import com.ravingarinc.biomachina.protocol.AnimationHandler
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.*

interface Vehicle {
    val uuid: UUID
    val entityId: Int

    val world: World

    fun buildAnimationController(handler: AnimationHandler) : AnimationController<*>

    fun create(location: Location)
    fun destroy()
    fun start(player: Player)
    fun stop(player: Player)
    fun tick()

    fun sync()
    fun isRunning() : Boolean

    fun boundingBox() : Interaction?
    fun interact(manager: VehicleManager, player: Player, hand: EquipmentSlot)
    fun mount(player: Player) : Boolean
    fun dismount(player: Player) : Boolean

    fun dismountAll()

    fun forceDismount(player: Player) : Boolean
}