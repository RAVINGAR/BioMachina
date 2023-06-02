package com.ravingarinc.biomachina.persistent.json

import com.ravingarinc.biomachina.vehicle.VehiclePart
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f

object Vector3fSerializer : KSerializer<Vector3f> {
    private val delegateSerializer = FloatArraySerializer()
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("Vector3f", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Vector3f) {
        encoder.encodeSerializableValue(delegateSerializer, floatArrayOf(value.x, value.y, value.z))
    }
    override fun deserialize(decoder: Decoder): Vector3f {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return Vector3f(array[0], array[1], array[2])
    }
}

object AxisAngle4fSerializer : KSerializer<AxisAngle4f> {
    private val delegateSerializer = FloatArraySerializer()
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("AxisAngle4f", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: AxisAngle4f) {
        encoder.encodeSerializableValue(delegateSerializer, floatArrayOf(value.angle, value.x, value.y, value.z))
    }
    override fun deserialize(decoder: Decoder): AxisAngle4f {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return AxisAngle4f(array[0], array[1], array[2], array[3])
    }
}

object QuaternionfSerializer : KSerializer<Quaternionf> {
    private val delegateSerializer = FloatArraySerializer()
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = SerialDescriptor("Quaternionf", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Quaternionf) {
        encoder.encodeSerializableValue(delegateSerializer, floatArrayOf(value.x, value.y, value.z, value.w))
    }
    override fun deserialize(decoder: Decoder): Quaternionf {
        val array = decoder.decodeSerializableValue(delegateSerializer)
        return Quaternionf(array[0], array[1], array[2], array[3])
    }
}
@Serializable
class VehicleTypeSurrogate(val identifier: String, val chassisHeight: Float, val chassis: VehiclePart, val frontWheels: List<VehiclePart>, val rearWheels: List<VehiclePart>) {
    init {
        require(identifier.isNotEmpty())
    }
}