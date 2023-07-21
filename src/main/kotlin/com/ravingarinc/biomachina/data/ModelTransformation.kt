package com.ravingarinc.biomachina.data

import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

sealed class ModelTransformation {
    abstract val origin: Vector3f

    abstract var yaw: Float
    abstract var pitch: Float
    abstract var roll: Float

    abstract val scale: Vector3f

    abstract var inverted: Boolean

    abstract fun copy() : ModelTransformation

    fun override(vector: ModelTransformation) {
        this.origin.set(vector.origin)
        this.scale.set(vector.scale)
        this.inverted = vector.inverted
        this.yaw = vector.yaw
        this.pitch = vector.pitch
        this.roll = vector.roll
    }

    override fun hashCode(): Int {
        return Objects.hash(origin, yaw, pitch, roll, scale, inverted)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        } else if(other == null) {
            return false
        } else if(other is ModelTransformation) {
            return other.origin == this.origin && other.yaw == this.yaw && other.pitch == this.pitch && other.roll == this.roll && other.scale == this.scale && other.inverted == this.inverted
        }
        return false
    }
}

fun Vector3f.copy() : Vector3f {
    return Vector3f(this.x, this.y, this.z)
}

fun AxisAngle4f.copy() : AxisAngle4f {
    return AxisAngle4f(this.angle, this.x, this.y, this.z)
}

fun Quaternionf.copy() : Quaternionf {
    return Quaternionf(this)
}

fun emptyTransformation() : ModelTransformation {
    return EmptyTransformation()
}

class EmptyTransformation : ModelTransformation() {
    override val origin: Vector3f = Vector3f(0F, 0F, 0F)
    override val scale: Vector3f = Vector3f(1F, 1F, 1F)
    override var yaw: Float = 0F
    override var pitch: Float = 0F
    override var roll: Float = 0F
    override var inverted: Boolean = false

    override fun copy(): ModelTransformation {
        return EmptyTransformation()
    }
}

