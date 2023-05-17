package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModule
import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.api.withModule
import com.ravingarinc.biomachina.data.VehicleType
import com.ravingarinc.biomachina.persistent.PersistenceHandler
import com.ravingarinc.biomachina.protocol.AnimationHandler
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class VehicleManager(plugin: RavinPlugin) : SuspendingModule(VehicleManager::class.java, plugin) {
    private var nextModelData: Int = 1 //todo save this model data to a file so that adding new vehicles doesn't mess with it
    private val cachedVehicles: MutableMap<UUID, Vehicle> = ConcurrentHashMap()

    private val cachedInteractions: MutableMap<Interaction, UUID> = ConcurrentHashMap()
    private val vehiclesById: MutableMap<Int, UUID> = ConcurrentHashMap()
    private val mountedPlayers: MutableMap<Player, UUID> = ConcurrentHashMap()

    private lateinit var animationHandler: AnimationHandler

    private lateinit var runner: BukkitTask
    override suspend fun suspendLoad() {
        animationHandler = plugin.getModule(AnimationHandler::class.java)
        plugin.withModule(PersistenceHandler::class.java) { this.getVehicleConfigs().forEach { section ->
            val id = section.name.lowercase()
            if(MotorVehicle.Factory.hasType(id)) {
                warn("Could not load one vehicle type configuration called '$id' as that identifier is already being used!")
            } else {
                loadVehicleType(id, section)?.let { MotorVehicle.Factory.add(it) }
            }
        } }
    }

    private fun loadVehicleType(id: String, section: ConfigurationSection) : VehicleType? {
        val seats = section.getInt("passenger_seats", 0)
        val chassisModel : String? = section.getString("chassis.model")?.replace(".json", "");
        if(chassisModel == null) {
            warn("Could not find chassis.model for vehicle type '$id'")
            return null
        }
        val wheelModel = section.getString("wheels.model")?.replace(".json", "");
        if(wheelModel == null) {
            warn("Could not find wheels.model for vehicle type '$id'")
            return null
        }
        val frontAmount = section.getInt("wheels.front_amount", 0)
        val rearAmount = section.getInt("wheels.rear_amount", 0)

        //todo read stats and stuff
        val cmd = getNextModelData()
        return VehicleType(id, seats, chassisModel, wheelModel, "${cmd}0".toInt(), "${cmd}1".toInt(), frontAmount, rearAmount)
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
        return nextModelData++
    }

    fun createVehicle(type: VehicleType, spawnLocation: Location) {
        val vehicle = type.build()
        vehicle.create(spawnLocation)
        cachedVehicles[vehicle.uuid] = vehicle
        vehicle.boundingBox()?.let {
            cachedInteractions[it] = vehicle.uuid
        }
        vehiclesById[vehicle.entityId] = vehicle.uuid //sync only
        animationHandler.registerVehicle(vehicle)

        //todo save in database here
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
        //runner.cancel()
        cachedVehicles.forEach {
            it.value.destroy()
        }
        cachedVehicles.clear()
        cachedInteractions.clear()
        MotorVehicle.Factory.clear()
        // todo save custom model datas
    }


}