package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.api.I
import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.vehicle.VehiclePart.Type
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
                           partBuilder: MutableMap<Type, List<VehiclePart>>.() -> Unit) {
    protected val chassis = VehiclePart(ModelData(chassisModelData))
    protected val parts: Map<Type, List<VehiclePart>> = buildMap {
        partBuilder.invoke(this)
        this[Type.CHASSIS] = listOf(chassis)
    }

    /**
     * The boat represents the centre of the vehicle at all times. All vehicle components are moved relative
     * to the driver seat.
     */


    var chassisHeight = 0.5F


    /**
     * Create a vehicle from this type.
     */
    abstract fun build(uuid: UUID = UUID.randomUUID()): MotorVehicle

    fun chassis() : VehiclePart {
        return chassis
    }

    /**
     * Gets all part models for the given type or an empty list if no such part exists
     */
    fun parts(part: Type): List<VehiclePart> {
        return parts[part] ?: return emptyList();
    }

    fun allParts() : Map<Type, List<VehiclePart>> {
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



