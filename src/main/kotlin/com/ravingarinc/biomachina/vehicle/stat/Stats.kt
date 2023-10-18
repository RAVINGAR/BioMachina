package com.ravingarinc.biomachina.vehicle.stat

import com.ravingarinc.biomachina.persistent.Properties

/**
 * Represents the maximum speed of the vehicle
 */
object Speed : StatProvider<Float, FloatStat>("speed", { prop, base ->
    FloatStat(prop.minSpeed.value, base, prop.maxSpeed.value)
})


/**
 * Represents the acceleration of the vehicle
 */
object Acceleration : StatProvider<Float, FloatStat>("acceleration", { prop, base ->
    FloatStat(prop.minAccel.value, base, prop.maxAccel.value)
})



/**
 * Represents the capability of a vehicle to traverse vertically up terrain
 */
object TerrainHeight : StatProvider<Float, FloatStat>("terrain_height", { prop, base ->
    FloatStat(prop.minTerrainHeight.value, base, prop.maxTerrainHeight.value)
})

object BrakingPower : StatProvider<Float, FloatStat>("braking_power", { prop, base ->
    FloatStat(prop.minBrakingPower.value, base, prop.maxBrakingPower.value)
})

open class StatProvider<T, out U: Stat<T>>(val identifier: String, private val provider: (Properties, T) -> U) {
    fun create(properties: Properties, base: T) : U {
        return provider.invoke(properties, base)
    }
}