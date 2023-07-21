package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.vehicle.ModelPart
import com.ravingarinc.biomachina.vehicle.Part.Type
import com.ravingarinc.biomachina.vehicle.VehicleManager
import com.ravingarinc.biomachina.vehicle.VehicleType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.bukkit.configuration.ConfigurationSection
import org.joml.Vector3f
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

class MotorVehicleType(identifier: String,
                       passengerSeats: Int,
                       chassisPath: String,
                       val wheelPath: String,
                       chassisModelData: Int,
                       wheelModelData: Int,
                       frontWheelAmount: Int,
                       rearWheelAmount: Int) : VehicleType(identifier, passengerSeats, chassisPath, chassisModelData, {
    this[Type.FRONT_WHEEL] = buildList {
        for(i in 0 until frontWheelAmount) {
            this.add(ModelPart(ModelData(wheelModelData)))
        }
    }
    this[Type.REAR_WHEEL] = buildList {
        for(i in 0 until rearWheelAmount) {
            this.add(ModelPart(ModelData(wheelModelData)))
        }
    }
}) {
    override fun build(uuid: UUID): MotorVehicle {
        //todo more things here? like load stats
        return MotorVehicle(uuid, this)
    }

    override fun reload(manager: VehicleManager) {
        Factory.reload(manager, this)
    }

    override fun save(manager: VehicleManager) {
        Factory.save(manager, this)
    }

    companion object Factory : VehicleType.Factory<MotorVehicleType> {
        @OptIn(ExperimentalSerializationApi::class)
        override fun load(manager: VehicleManager, section: ConfigurationSection): MotorVehicleType? {
            val id = section.name
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

            val file = File(manager.jsonFolder, "${id}.json")
            if(file.exists()) {
                FileInputStream(file).use {
                    val surrogate: MotorVehicleTypeSurrogate = Json.decodeFromStream(it)
                    surrogate
                }.let {
                    val wheelCmd =
                        if(it.frontWheels.isNotEmpty()) it.frontWheels[0].model.data
                        else (if(it.rearWheels.isNotEmpty()) it.rearWheels[0].model.data else -1)

                    val type = MotorVehicleType(id, seats, chassisModel, wheelModel, it.chassis.model.data, wheelCmd, frontAmount, rearAmount)

                    type.chassis.override(it.chassis)
                    val frontWheels = type.parts(Type.FRONT_WHEEL)
                    for(i in frontWheels.indices) {
                        if(i < it.frontWheels.size) {
                            frontWheels[i].override(it.frontWheels[i])
                        }
                    }
                    val rearWheels = type.parts(Type.REAR_WHEEL)
                    for(i in rearWheels.indices) {
                        if(i < it.rearWheels.size) {
                            rearWheels[i].override(it.rearWheels[i])
                        }
                    }
                    type.collisionBox.override(it.collision)
                    return type
                }
            } else {
                val cmd = manager.getNextModelData()
                val type = MotorVehicleType(id, seats, chassisModel, wheelModel, "${cmd}0".toInt(), "${cmd}1".toInt(), frontAmount, rearAmount)
                save(manager, type)
                return type
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun reload(manager: VehicleManager, type: MotorVehicleType) {
            val file = File(manager.jsonFolder, "${type.identifier}.json")
            if(file.exists()) {
                FileInputStream(file).use {
                    val surrogate: MotorVehicleTypeSurrogate = Json.decodeFromStream(it)
                    surrogate
                }.let {
                    type.chassis.override(it.chassis)
                    val frontWheels = type.parts(Type.FRONT_WHEEL)
                    for(i in frontWheels.indices) {
                        if(i < it.frontWheels.size) {
                            frontWheels[i].override(it.frontWheels[i])
                        }
                    }
                    val rearWheels = type.parts(Type.REAR_WHEEL)
                    for(i in rearWheels.indices) {
                        if(i < it.rearWheels.size) {
                            rearWheels[i].override(it.rearWheels[i])
                        }
                    }
                    type.collisionBox.override(it.collision)
                }
            } else {
                type.parts.values.forEach { list ->
                    list.forEach {
                        if(it is ModelPart) {
                            it.model.origin.set(Vector3f())
                            it.model.scale.set(Vector3f())
                            it.model.yaw = 0F
                            it.model.pitch = 0F
                            it.model.roll = 0F
                        }
                    }
                }
            }
        }

        override fun save(manager: VehicleManager, type: MotorVehicleType) {
            val file = File(manager.jsonFolder, "${type.identifier}.json")
            if(file.exists()) {
                if(!file.delete()) return
            }
            val writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)
            writer.use {
                val json = Json.encodeToString(MotorVehicleTypeSurrogate(type.identifier, type.chassis, type.collisionBox, type.parts(Type.FRONT_WHEEL), type.parts(Type.REAR_WHEEL)))
                val gson = GsonBuilder().setPrettyPrinting().create()
                gson.toJson(JsonParser.parseString(json), it)
            }
            return
        }

    }
}

@Serializable
class MotorVehicleTypeSurrogate(val identifier: String, val chassis: ModelPart, val collision: ModelPart, val frontWheels: List<ModelPart>, val rearWheels: List<ModelPart>) {
    init {
        require(identifier.isNotEmpty())
    }
}