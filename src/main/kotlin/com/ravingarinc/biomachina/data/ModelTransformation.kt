package com.ravingarinc.biomachina.data

import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.Objects

sealed class ModelTransformation {
    abstract var origin: Vector3f
    abstract var leftRotation: Quaternionf
    abstract var rightRotation: Quaternionf
    abstract var scale: Vector3f

    abstract fun copy() : ModelTransformation

    override fun hashCode(): Int {
        var hash = 7
        hash = 11 * hash + Objects.hashCode(this.origin)
        hash = 11 * hash + Objects.hashCode(this.leftRotation)
        hash = 11 * hash + Objects.hashCode(this.scale)
        hash = 11 * hash + Objects.hashCode(this.rightRotation)
        return hash;
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        } else if(other == null) {
            return false
        } else if(other is ModelTransformation) {
            return other.origin == this.origin && other.leftRotation == this.leftRotation && other.rightRotation == this.rightRotation && other.scale == this.scale
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
    override var origin: Vector3f = Vector3f(0F, 0F, 0F)
    override var leftRotation: Quaternionf = Quaternionf()
    override var rightRotation: Quaternionf = Quaternionf()
    override var scale: Vector3f = Vector3f(1F, 1F, 1F)

    override fun copy(): ModelTransformation {
        return EmptyTransformation()
    }
}

