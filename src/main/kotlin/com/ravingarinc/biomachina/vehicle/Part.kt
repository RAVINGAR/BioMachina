package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.biomachina.data.ModelData
import kotlinx.serialization.Serializable

/**
 * Parts that a vehicle may have. Not all vehicles have all parts. However, it can be assumed that
 * a vehicle will always have a chassis
 */
abstract class Part() {
    abstract fun override(part: Part)
    class Type<T : Part>(val display: String) {
        companion object {
            val CHASSIS = Type<ModelPart>("Chassis")
            val ENGINE = Type<FunctionPart>("Engine")
            val FRONT_WHEEL = Type<ModelPart>("Front Wheel")
            val REAR_WHEEL = Type<ModelPart>("Rear Wheel")
            val COLLISION = Type<ModelPart>("Collision Box")
        }
    }
}

/**
 * Represents a vehicle part that represents a physically summonable object.
 */
@Serializable
class ModelPart(val model: ModelData) : Part() {
    override fun override(part: Part) {
        if(part is ModelPart) {
            model.override(part.model)
        }
    }
}

class FunctionPart() : Part() {
    override fun override(part: Part) {
        // Todo
    }
}

