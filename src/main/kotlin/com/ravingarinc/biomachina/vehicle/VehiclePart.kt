package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.biomachina.data.ModelData
import kotlinx.serialization.Serializable

/**
 * Parts that a vehicle may have. Not all vehicles have all parts. However, it can be assumed that
 * a vehicle will always have a chassis
 */
@Serializable
open class VehiclePart(val model: ModelData) {

    fun override(part: VehiclePart) {
        model.override(part.model)
    }
    enum class Type(val display: String) {
        CHASSIS("Chassis"),
        ENGINE("Engine"),
        FRONT_WHEEL("Front Wheel"),
        REAR_WHEEL("Rear Wheel");
    }
}

