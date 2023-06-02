package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.AnimationHandler
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicleType
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.*

interface Vehicle {
    val uuid: UUID
    val entityId: Int
    val type: VehicleType
    val world: World



    fun buildAnimationController(handler: AnimationHandler) : AnimationController<*>

    fun create(location: Location)
    fun destroy()
    fun start(player: Player)
    fun stop(player: Player)
    fun tick()

    fun update()

    fun sync()
    fun isRunning() : Boolean

    fun boundingBox() : Interaction?
    fun interact(manager: VehicleManager, player: Player, hand: EquipmentSlot)
    fun mount(player: Player) : Boolean
    fun dismount(player: Player) : Boolean

    fun dismountAll()

    fun forceDismount(player: Player) : Boolean

    object Factory {
        //todo move this
        // see https://blog.logrocket.com/understanding-kotlin-design-patterns/#factory-abstract-factory-provider-model-kotlin-method
        private val vehicleTypes: MutableMap<String, VehicleType> = HashMap()
        private val vehicleFactories: Map<String, VehicleType.Factory<*>> = buildMap {
            this["motor_vehicle"] = MotorVehicleType.Factory
            this["motor"] = MotorVehicleType.Factory
            this["motorvehicle"] = MotorVehicleType.Factory
            //this["bike"] = BikeType.Factory
            //this["aircraft"] = AircraftType.Factory
        }

        fun getFactory(typeName: String) : VehicleType.Factory<*> {
            return vehicleFactories[typeName.lowercase()] ?: VehicleType.UnknownFactory
        }

        fun getType(typeName: String) : VehicleType? {
            return vehicleTypes[typeName.lowercase()];
        }
        fun clear() {
            vehicleTypes.clear()
        }

        fun hasType(typeName: String) : Boolean {
            return vehicleTypes.containsKey(typeName)
        }

        fun getTypes() : Set<String> {
            return vehicleTypes.keys
        }

        fun add(type: VehicleType) {
            vehicleTypes[type.identifier] = type
        }
    }
}