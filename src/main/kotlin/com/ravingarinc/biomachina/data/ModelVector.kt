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
class ModelVector(
    override var origin: Vector3f = Vector3f(),
    @SerialName("lRot") override var leftRotation: Quaternionf = Quaternionf(),
    @SerialName("rRot") override var rightRotation: Quaternionf = Quaternionf(),
    override var scale: Vector3f = Vector3f(1F, 1F, 1F)
) : ModelTransformation() {

    override fun copy() : ModelVector {
        return ModelVector(Vector3f(origin), Quaternionf(leftRotation), Quaternionf(rightRotation), Vector3f(scale))
    }
}