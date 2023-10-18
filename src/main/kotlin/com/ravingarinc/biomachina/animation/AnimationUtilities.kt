package com.ravingarinc.biomachina.animation

import org.bukkit.util.Vector
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * This contains quick caches for common animation tropes
 */
object AnimationUtilities {
    const val FULL_ROTATION = (Math.PI * 2).toFloat()
    private val squareRootCache: MutableMap<Float, Float> = Hashtable()

    fun quickSqrt(squared: Float) : Float {
        return squareRootCache.computeIfAbsent(squared) {
            return@computeIfAbsent sqrt(it)
        }
    }

    fun quickSine(radians: Float) : Float {
        TODO()
    }

    fun quickCosine(radians: Float) : Float {
        TODO()
    }
}

/**
 * Rotate this vector by the given yaw and pitch in radians. Returns the same vector
 */
fun Vector.rotate(yaw: Float, pitch: Float) : Vector {
    val cosYaw = cos(yaw)
    val cosPitch = cos(pitch)
    val sinYaw = sin(yaw)
    val sinPitch = sin(pitch)
    val initialX: Double = this.x
    val initialY: Double = this.y
    val tempX = initialX * cosPitch - initialY * sinPitch
    val initialZ: Double = this.z
    z = initialZ * cosYaw - tempX * sinYaw
    x = initialZ * sinYaw + tempX * cosYaw
    y = initialX * sinPitch + initialY * cosPitch
    return this
}