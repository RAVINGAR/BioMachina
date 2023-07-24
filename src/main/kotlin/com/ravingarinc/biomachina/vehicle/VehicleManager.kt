package com.ravingarinc.biomachina.vehicle

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModule
import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.animation.AnimationHandler
import com.ravingarinc.biomachina.api.Ticker
import com.ravingarinc.biomachina.api.withModule
import com.ravingarinc.biomachina.data.editor.EditorSession
import com.ravingarinc.biomachina.persistent.PersistenceHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.bukkit.Location
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class VehicleManager(plugin: RavinPlugin) : SuspendingModule(VehicleManager::class.java, plugin) {
    private val usedModelData: MutableSet<Int> = HashSet()
    private var nextModelData: Int = 1 //todo save this model data to a file so that adding new vehicles doesn't mess with it
    private val cachedVehicles: MutableMap<UUID, Vehicle> = ConcurrentHashMap()

    private val cachedInteractions: MutableMap<Interaction, UUID> = ConcurrentHashMap()
    private val vehiclesById: MutableMap<Int, UUID> = ConcurrentHashMap()
    private val mountedPlayers: MutableMap<Player, UUID> = ConcurrentHashMap()

    private val editorSessions: MutableMap<UUID, EditorSession> = Hashtable()

    private lateinit var ticker: VehicleTicker

    val jsonFolder = File(plugin.dataFolder, "json")

    // TODO Find out a better to avoid having all these HASHMAPS. But also still have

    private lateinit var animationHandler: AnimationHandler
    override suspend fun suspendLoad() {
        animationHandler = plugin.getModule(AnimationHandler::class.java)
        plugin.withModule(PersistenceHandler::class.java) { this.getVehicleConfigs().forEach { section ->
            val id = section.name.lowercase()
            if(Vehicle.Factory.hasType(id)) {
                warn("Could not load one vehicle type configuration called '$id' as that identifier is already being used!")
            } else {
                val type = section.getString("type")
                if(type == null) {
                    warn("Could not find 'type' option for vehicle type configuration called '$id'!")
                } else {
                    Vehicle.Factory.getFactory(type).load(this@VehicleManager, section)?.let {
                        Vehicle.Factory.add(it)
                        it.allParts().values.forEach { list ->
                            list.forEach { part -> if(part is ModelPart) usedModelData.add(part.model.data) }
                        }
                    }
                }
            }
        } }
        ticker = VehicleTicker(plugin, cachedVehicles.values)
        ticker.start(5)
    }

    fun registerMount(player: Player, vehicle: Vehicle) {
        this.mountedPlayers[player] = vehicle.uuid
    }

    fun unregisterMount(player: Player) {
        this.mountedPlayers.remove(player)
    }

    fun getMount(player: Player) : Vehicle? {
        val uuid = mountedPlayers[player] ?: return null
        return cachedVehicles[uuid]
    }

    fun getVehicle(uuid: UUID) : Vehicle? {
        return cachedVehicles[uuid]
    }

    fun getVehicleById(id: Int) : Vehicle? {
        val uuid = vehiclesById[id] ?: return null
        return cachedVehicles[uuid]
    }

    fun getVehicleFromInteraction(interaction: Interaction) : Vehicle? {
        val uuid = cachedInteractions[interaction] ?: return null
        return cachedVehicles[uuid]
    }

    fun getNextModelData() : Int {
        var cmd = nextModelData++
        while(usedModelData.contains("${cmd}0".toInt())) {
            cmd = nextModelData++
        }
        return cmd
    }

    fun hasEditorSession(player: Player) : Boolean {
        return editorSessions.containsKey(player.uniqueId)
    }

    fun openEditorSession(player: Player, type: VehicleType) {
        val session = EditorSession(plugin, type, player)
        editorSessions[player.uniqueId] = session
        session.open()
    }

    fun removeEditorSession(player: Player) {
        editorSessions.remove(player.uniqueId)
    }

    fun discardEditorSession(player: Player) {
        val session = editorSessions[player.uniqueId]
        session?.discard()
    }

    /**
     * Return a copy of the currently cached vehicles
     */
    fun getVehicles() : Collection<Vehicle> {
        return ArrayList(cachedVehicles.values)
    }

    fun createVehicle(type: VehicleType, spawnLocation: Location) : Vehicle {
        val vehicle = type.build()
        vehicle.create(spawnLocation)
        cachedVehicles[vehicle.uuid] = vehicle
        vehicle.boundingBox()?.let {
            cachedInteractions[it] = vehicle.uuid
        }
        vehiclesById[vehicle.entityId] = vehicle.uuid //sync only
        animationHandler.registerVehicle(vehicle)

        //todo save in database here
        return vehicle
    }

    fun removeVehicle(vehicle: Vehicle) {
        animationHandler.unregisterVehicle(vehicle)
        vehicle.dismountAll()
        vehiclesById.remove(vehicle.entityId)
        cachedInteractions.remove(vehicle.boundingBox())
        cachedVehicles.remove(vehicle.uuid)
        vehicle.destroy()
    }

    fun loadVehicle(uuid: UUID, type: VehicleType, location: Location) {
        // also load mods here!
    }

    override suspend fun suspendCancel() {
        mountedPlayers.keys.forEach {
            it.eject()
        }
        mountedPlayers.clear()
        editorSessions.forEach { (_, session) ->
            session.discard()
        }
        editorSessions.clear()
        ticker.cancel()
        cachedVehicles.forEach {
            it.value.destroy()
        }
        cachedVehicles.clear()
        cachedInteractions.clear()
        Vehicle.Factory.clear()
        nextModelData = 0
        // todo save custom model datas
    }


    class VehicleTicker(plugin: RavinPlugin, private val vehicles: Collection<Vehicle>) : Ticker(plugin, 5.ticks, context = plugin.minecraftDispatcher) {
        private val semaphore: Semaphore = Semaphore(4)
        override suspend fun CoroutineScope.tick() {
            ArrayList(vehicles).forEach {
                semaphore.acquire()
                scope.launch {
                    try {
                        if(!it.isDestroyed) {
                            it.tick()
                        }
                    }
                    finally {
                        semaphore.release()
                    }
                }
            }
        }
    }
}