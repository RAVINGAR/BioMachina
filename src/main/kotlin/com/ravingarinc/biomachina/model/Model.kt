package com.ravingarinc.biomachina.model

import org.bukkit.World
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

interface Model {
    val parent: Model?

    /**
     * Spawn the entity representing this model
     */
    fun create(x: Double, y: Double, z: Double, world: World)

    /**
     * Remove the entity representing this model from the world
     */
    fun destroy()

    fun forEach(consumer: (Model) -> Unit)

    fun calculateRotationOffset(x: Float, z: Float, rads: Float): Vector3f {
        // Calculate the sine and cosine of the angle
        val sinY = sin(rads)
        val cosY = cos(rads)

        // Calculate the x and z coordinates based on the rotation
        return Vector3f(
            (cosY * x + sinY * z) - x,
            0F,
            (-sinY * x + cosY * z) - z)
    }
}