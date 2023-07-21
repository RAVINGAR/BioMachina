@file:UseSerializers(Vector3fSerializer::class, QuaternionfSerializer::class)

package com.ravingarinc.biomachina.data

import com.ravingarinc.biomachina.persistent.json.QuaternionfSerializer
import com.ravingarinc.biomachina.persistent.json.Vector3fSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.joml.Vector3f
import java.util.*

@Serializable
class ModelData(var data: Int,
                override val origin: Vector3f = Vector3f(),
                override var yaw: Float = 0F,
                override var pitch: Float = 0F,
                override var roll: Float = 0F,
                override val scale: Vector3f = Vector3f(1F, 1F, 1F),
                override var inverted: Boolean = false) : ModelTransformation() {
    override fun copy() : ModelData {
        return ModelData(data, Vector3f(origin), yaw, pitch, roll, Vector3f(scale), inverted)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other is ModelData && other.data == this.data)
    }

    override fun hashCode(): Int {
        return Objects.hash(data, origin, yaw, pitch, roll, scale, inverted)
    }
}
