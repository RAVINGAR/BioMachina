package com.ravingarinc.biomachina.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerOptions
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModuleListener
import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.AnimationTicker
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import com.ravingarinc.biomachina.viewer.Individual
import com.ravingarinc.biomachina.viewer.Viewer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AnimationHandler(plugin: RavinPlugin) : SuspendingModuleListener(AnimationHandler::class.java, plugin) {
    private val protocol = ProtocolLibrary.getProtocolManager()
    private lateinit var manager : VehicleManager
    private val exemptIds: MutableMap<Int, AnimationController<*>> = ConcurrentHashMap()
    private val controllers: MutableMap<UUID, AnimationController<*>> = ConcurrentHashMap()

    private val viewers: MutableMap<UUID, Individual> = ConcurrentHashMap()

    private lateinit var controllerTicker: AnimationTicker
    override suspend fun suspendLoad() {
        manager = plugin.getModule(VehicleManager::class.java)

        plugin.server.onlinePlayers.forEach {
            viewers[it.uniqueId] = Individual(it)
        }

        protocol.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, listOf(PacketType.Play.Server.SPAWN_ENTITY), ListenerOptions.ASYNC) {
            override fun onPacketSending(event: PacketEvent?) {
                // do what we did in actors plugin, listen for spawn and remove packets and add and remove listeners as appropriately
                val id = event!!.packet.integers.readSafely(0)
                exemptIds[id]?.addViewer(getIndividual(event.player))
            }
        })

        // todo we need to prevent any unwanted packets form being sent to the client!
        // just like in actors!
        protocol.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, listOf(PacketType.Play.Server.ENTITY_DESTROY), ListenerOptions.ASYNC) {
            override fun onPacketSending(event: PacketEvent?) {
                event!!.packet.intLists.readSafely(0)?.let {
                    for(id in it) exemptIds[id]?.removeViewer(getIndividual(event.player))
                }
            }
        })
        controllerTicker = AnimationTicker(plugin, 8, controllers.values)
        controllerTicker.start(5)

        super.suspendLoad()
    }

    override suspend fun suspendCancel() {
        super.suspendCancel()
        controllerTicker.cancel()
        protocol.removePacketListeners(plugin)

        controllers.values.forEach {
            it.dispose(this)
        }
        controllers.clear()
        viewers.values.forEach {
            it.destroy()
        }
        viewers.clear()
        exemptIds.clear()
    }

    fun registerVehicle(vehicle: Vehicle) {
        if(controllers.containsKey(vehicle.uuid)) {
            warn("Attempted to register vehicle inside AnimationHandler twice! This should not have occurred!")
            return
        }
        val controller = vehicle.buildAnimationController(this)
        controllers[vehicle.uuid] = controller
        vehicle.world.players.forEach { controller.addViewer(getIndividual(it)) }
    }

    fun unregisterVehicle(vehicle: Vehicle) {
        controllers.remove(vehicle.uuid)?.dispose(this)
    }

    fun addExemption(id: Int, controller: AnimationController<*>) {
        exemptIds[id] = controller
    }

    fun removeExemption(id: Int) {
        exemptIds.remove(id)
    }

    fun getIndividual(player: Player): Individual {
        return viewers.getOrPut(player.uniqueId) { Individual(player) }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        viewers.remove(event.player.uniqueId)?.destroy()
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        viewers.remove(event.player.uniqueId)?.destroy()
    }
}