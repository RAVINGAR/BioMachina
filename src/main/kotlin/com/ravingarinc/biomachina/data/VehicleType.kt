package com.ravingarinc.biomachina.data

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.vehicle.MotorVehicle
import java.util.*

/**
 * Stores details regarding a specific vehicle. These details can be modified via an interactive GUI.
 * Construction parameters are read via the ConfigManager, meanwhile, ModelData for each VehicleType
 * is stored in a json folder located in the plugin directory/json
 */
class VehicleType(val identifier: String,
                  val passengerSeats: Int,
                  val chassisPath: String,
                  val wheelPath: String,
                  chassisModelData: Int,
                  wheelModelData: Int,
                  frontWheelAmount: Int,
                  rearWheelAmount: Int) {
    val wheelFactor = 1F
    val frontWheels: List<ModelData> = buildList {
        for(i in 0 until frontWheelAmount) {
            this.add(ModelData(wheelModelData))
        }
    }
    val rearWheels: List<ModelData> = buildList {
        for(i in 0 until rearWheelAmount) {
            this.add(ModelData(wheelModelData))
        }
    }
    /**
     * The boat represents the centre of the vehicle at all times. All vehicle components are moved relative
     * to the driver seat.
     */
    val chassis = ModelData(chassisModelData)

    /**
     * Load values from saved configuration
     */
    fun load(plugin: RavinPlugin) {

    }

    fun build(uuid: UUID = UUID.randomUUID()): MotorVehicle {
        //todo more things here? like load stats
        return MotorVehicle(uuid, this)
    }
}
