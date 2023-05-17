@file:UseSerializers(Vector3fSerializer::class, QuaternionfSerializer::class)

package com.ravingarinc.biomachina.data

import com.ravingarinc.biomachina.persistent.json.AxisAngle4fSerializer
import com.ravingarinc.biomachina.persistent.json.QuaternionfSerializer
import com.ravingarinc.biomachina.persistent.json.Vector3fSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f

@Serializable
class ModelData(val customModelData: Int,
                override var origin: Vector3f = Vector3f(),
                @SerialName("lRot") override var leftRotation: Quaternionf = Quaternionf(),
                @SerialName("rRot") override var rightRotation: Quaternionf = Quaternionf(),
                override var scale: Vector3f = Vector3f(1F, 1F, 1F)) : ModelTransformation() {
    override fun copy() : ModelData {
        return ModelData(customModelData, Vector3f(origin), Quaternionf(leftRotation), Quaternionf(rightRotation), Vector3f(scale))
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other is ModelData && other.customModelData == this.customModelData)
    }

    override fun hashCode(): Int {
        var result = customModelData
        result = 31 * result + origin.hashCode()
        result = 31 * result + leftRotation.hashCode()
        result = 31 * result + rightRotation.hashCode()
        result = 31 * result + scale.hashCode()
        return result
    }


}
