package com.ravingarinc.biomachina.model.api

import com.ravingarinc.biomachina.api.toRadians
import org.joml.Quaternionf
import org.joml.Vector3f

interface VectorModel {
    val origin: Vector3f
    val leftRotation: Quaternionf
    val rightRotation: Quaternionf
    val scale: Vector3f

    val rotatingOrigin: Vector3f

    /**
     * Pitch rotates on the x-axis
     */
    var absPitch: Float
    /**
     * Yaw rotates on the y-axis
     */
    var absYaw: Float

    /**
     * Roll rotates on the z-axis
     */
    var absRoll: Float

    /**
     * Reapply the transformation
     */
    fun reapply()

    /**
     * Pitch rotates on the x-axis. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relPitch: Float

    /**
     * Yaw rotates on the y-axis. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relYaw: Float

    /**
     * Roll rotates on the z-axis. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relRoll: Float

    /**
     * Add this number of degrees to the relative pitch.
     */
    fun rotatePitch(degrees: Float) {
        val old = relPitch
        relPitch = (relPitch + degrees) % 360
        rightRotation.rotateX((relPitch - old).toRadians())
    }

    /**
     * Add this number of degrees to the relative yaw
     */
    fun rotateYaw(degrees: Float) {
        val old = relYaw
        relYaw = (relYaw + degrees) % 360
        rightRotation.rotateY((relYaw - old).toRadians())
    }

    /**
     * Add this number of degrees to the relative roll
     */
    fun rotateRoll(degrees: Float) {
        val old = relRoll
        relRoll = (relRoll + degrees) % 360
        rightRotation.rotateZ((relRoll - old).toRadians())
    }

    fun totalYaw() : Float {
        return relYaw
    }

    fun totalPitch() : Float {
        return relPitch
    }

    fun totalRoll() : Float {
        return relRoll
    }
}