package com.ravingarinc.biomachina.vehicle

import com.ravingarinc.biomachina.data.ModelData
import com.ravingarinc.biomachina.data.ModelVector
import com.ravingarinc.biomachina.model.BlockDisplayModel
import com.ravingarinc.biomachina.model.DisplayModel
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World

/**
 * Parts that a vehicle may have. Not all vehicles have all parts. However, it can be assumed that
 * a vehicle will always have a chassis
 */
abstract class Part() {
    abstract fun override(part: Part)
    class Type<T : Part>(val display: String) {
        companion object {
            val CHASSIS = Type<CollidablePart>("Chassis")
            val ENGINE = Type<FunctionPart>("Engine")
            val FRONT_WHEEL = Type<CollidablePart>("Front Wheel")
            val REAR_WHEEL = Type<CollidablePart>("Rear Wheel")
        }
    }
}

@Serializable
class CollidablePart(override val model: ModelData, val collision: ModelVector) : ModelPart() {
    override fun override(part: Part) {
        if(part is CollidablePart) {
            collision.override(part.collision)
        }
        super.override(part)
    }

    fun createCollisionModel(location: Location) : DisplayModel<*> {
        return createCollisionModel(location.x, location.y, location.z, location.world)
    }

    fun createCollisionModel(x: Double, y: Double, z: Double, world: World) : DisplayModel<*> {
        val model = BlockDisplayModel(collision, Material.GLASS, null)
        model.create(x, y, z, world)
        return model
    }
}

@Serializable
class NonCollidablePart(override val model: ModelData) : ModelPart()

/**
 * Represents a vehicle part that represents a physically summonable object.
 */

sealed class ModelPart() : Part() {
    abstract val model: ModelData
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

