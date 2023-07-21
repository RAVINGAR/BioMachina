package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.api.I
import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.vehicle.Part.Type
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicle
import org.bukkit.configuration.ConfigurationSection
import java.util.*
import java.util.logging.Level

/**
 * Stores details regarding a specific vehicle. These details can be modified via an interactive GUI.
 * Construction parameters are read via the ConfigManager, meanwhile, ModelData for each VehicleType
 * is stored in a json folder located in the plugin directory/json
 */

abstract class VehicleType(val identifier: String,
                           val passengerSeats: Int,
                           val chassisPath: String,
                           chassisModelData: Int,
                           partBuilder: MutableMap<Type<*>, List<Part>>.() -> Unit) {
    val chassis = ModelPart(ModelData(chassisModelData))
    val collisionBox = ModelPart(ModelData(-1))
    val parts: Map<Type<*>, List<Part>> = buildMap {
        partBuilder.invoke(this)
        this[Type.CHASSIS] = listOf(chassis)
        this[Type.COLLISION] = listOf(collisionBox)
    }

    var height: Float = 1.0F

    /**
     * Create a vehicle from this type.
     */
    abstract fun build(uuid: UUID = UUID.randomUUID()): MotorVehicle

    /**
     * Gets all part models for the given type or an empty list if no such part exists. Somewhat computationally
     * expensive so use sparingly
     */
    inline fun <reified T : Part> parts(part: Type<T>): List<T> {
        val list = parts[part] ?: return emptyList()
        val newList = ArrayList<T>()
        list.forEach {
            if(it is T) newList.add(it)
        }
        return newList
    }

    fun allParts() : Map<Type<*>, List<Part>> {
        return parts
    }

    abstract fun reload(manager: VehicleManager)
    abstract fun save(manager: VehicleManager)

    interface Factory<T : VehicleType> {
        fun load(manager: VehicleManager, section: ConfigurationSection) : T?

        fun reload(manager: VehicleManager, type: T)

        fun save(manager: VehicleManager, type: T)
    }

    object UnknownFactory : Factory<VehicleType> {
        override fun load(manager: VehicleManager, section: ConfigurationSection): VehicleType? {
            I.log(Level.WARNING, "Unknown type of '${section.getString("type")}' for vehicle '${section.name}'")
            return null
        }

        override fun reload(manager: VehicleManager, type: VehicleType) {
            I.log(Level.SEVERE, ("Cannot reload vehicle type '${type.identifier}' for unknown type!"))
        }

        override fun save(manager: VehicleManager, type: VehicleType) {
            I.log(Level.SEVERE, ("Cannot save vehicle type '${type.identifier}' for unknown type!"))
        }
    }
}



