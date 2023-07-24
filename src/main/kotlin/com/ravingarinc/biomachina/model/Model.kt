package com.ravingarinc.biomachina.model

import org.bukkit.World
import org.joml.Matrix3f
import org.joml.Vector3f

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

    fun calculateRotationOffset(x: Float, y: Float, z: Float, yaw: Float, pitch: Float, roll: Float): Vector3f {
        // Construct the rotation matrix (yaw -> pitch -> roll)
        val rotationMatrix = Matrix3f()
            .rotateY(yaw)
            .rotateX(pitch)
            .rotateZ(roll)

        // Apply the rotation matrix to the original coordinates
        val resultVec = Vector3f()
        rotationMatrix.transform(Vector3f(x, y, z), resultVec)
        return Vector3f(resultVec.x - x, resultVec.y - y, resultVec.z - z)
    }

    /*
    fun calculateRotationOffset(x: Float, z: Float, yaw: Float): Vector3f {
        // Calculate the sine and cosine of the angle
        val sinY = sin(yaw)
        val cosY = cos(yaw)

        // Calculate the x and z coordinates based on the rotation
        return Vector3f(
            (cosY * x + sinY * z) - x,
            0F,
            (-sinY * x + cosY * z) - z)
    }
    */
}