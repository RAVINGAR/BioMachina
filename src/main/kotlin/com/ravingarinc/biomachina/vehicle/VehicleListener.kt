package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModuleListener
import org.bukkit.entity.Boat
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.spigotmc.event.entity.EntityDismountEvent

class VehicleListener(plugin: RavinPlugin) : SuspendingModuleListener(VehicleListener::class.java, plugin, true, VehicleManager::class.java) {
    private lateinit var manager: VehicleManager
    override suspend fun suspendLoad() {
        manager = plugin.getModule(VehicleManager::class.java)
        super.suspendLoad()
    }

    @EventHandler
    fun onInteraction(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        if(entity is Interaction) {
            manager.getVehicleFromInteraction(entity)?.interact(manager, event.player, event.hand)
        }
    }

    @EventHandler
    fun onDismount(event: EntityDismountEvent) {
        val entity = event.entity
        if(entity is Player) {
            manager.getMount(entity)?.let {
                it.dismount(entity)
                manager.unregisterMount(entity)
            }
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        manager.getMount(player)?.let {
            it.forceDismount(player)
            manager.unregisterMount(player)
        }
        if(manager.hasEditorSession(player)) {
            manager.discardEditorSession(player)
        }
    }

    @EventHandler
    fun onVehicleMove(event: VehicleMoveEvent) {
        val entity = event.vehicle
        if(entity is Boat) {
            manager.getVehicleById(event.vehicle.entityId)?.sync()
            /*
            val direction = event.to.toVector().subtract(event.from.toVector())
            if(direction.isZero) {
                return;
            }
            entity.velocity = direction.normalize().multiply(0.4)

            //val velocity = entity.passengers[0].velocity
            //entity.velocity = Vector(velocity.x * 20, 0.0, velocity.z * 20)8
            */
        }

        /*
        val boat = event.vehicle
        if(boat is Boat) {

            if(boat.passengers.isNotEmpty()) {

                val direction = event.to.toVector().subtract(event.from.toVector())
                if(direction.isZero) {
                    return;
                }
                boat.velocity = direction.normalize().multiply(0.4);*/
                //I.log(Level.WARNING, "Boat velocity '${boat.velocity}'")
                /*
                val passenger = boat.passengers[0]
                val velocity = passenger.velocity
                // interesting so the passengers velocity tends to align with where the player is looking
                /*

                 */
                val v = Vector(velocity.x * 20, boat.velocity.y, velocity.z * 20)
                boat.velocity = v


                //boat.velocity = Vector(velocity.x * 2, boat.velocity.y, velocity.z * 2)

                // or maybe you need to like change the passengers velocity?
                // proof that changing boat velocity doesnt do anything whilst player is in the boat

            }
        }*/
    }
}