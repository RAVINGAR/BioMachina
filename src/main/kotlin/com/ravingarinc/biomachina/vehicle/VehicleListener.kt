package com.ravingarinc.biomachina.vehicle

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.async.AsyncListenerHandler
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketListener
import com.github.shynixn.mccoroutine.bukkit.launch
import com.ravingarinc.api.I
import com.ravingarinc.api.module.ModuleListener
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModuleListener
import com.ravingarinc.api.module.log
import org.bukkit.entity.Boat
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.util.Vector
import org.spigotmc.event.entity.EntityDismountEvent
import org.spigotmc.event.entity.EntityMountEvent
import java.util.*
import java.util.logging.Level

class VehicleListener(plugin: RavinPlugin) : SuspendingModuleListener(VehicleListener::class.java, plugin) {
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
    }

    @EventHandler
    fun onVehicleMove(event: VehicleMoveEvent) {
        val entity = event.vehicle
        if(entity is Boat) {
            manager.getVehicleById(event.vehicle.entityId)?.sync()
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