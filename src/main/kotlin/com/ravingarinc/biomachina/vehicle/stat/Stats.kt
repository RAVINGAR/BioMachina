package com.ravingarinc.biomachina.vehicle.stat

import com.ravingarinc.biomachina.persistent.Properties

/**
 * Represents the maximum speed of the vehicle
 */

/*
object Speed : StatProvider<Int, IntStat>({ prop, base ->
    IntStat(prop.minSpeed.value, base, prop.maxSpeed.value)
})
*/

/**
 * Represents the acceleration of the vehicle
 */

/*
object Acceleration : StatProvider<Int, IntStat>({ prop, base ->
    IntStat(prop.minAccel.value, base, prop.maxAccel.value)
})
*/


/**
 * Represents the capability of a vehicle to traverse vertically up terrain
 */

/*
object TerrainApproachHeight : StatProvider<Float, FloatStat>({ _, base ->
    FloatStat(0.5F, base, 3.5F)
})

open class StatProvider<T, out U: Stat<T>>(private val provider: (Properties, T) -> U) {
    fun create(properties: Properties, base: T) : U {
        return provider.invoke(properties, base)
    }
}

 */