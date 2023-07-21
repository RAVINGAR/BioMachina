package com.ravingarinc.biomachina.model

import com.ravingarinc.biomachina.animation.AnimationUtilities
import org.joml.Quaternionf
import org.joml.Vector3f

interface VectorModel {

    val origin: Vector3f
    val leftRotation: Quaternionf
    val rightRotation: Quaternionf
    val scale: Vector3f

    val rotatingOrigin: Vector3f

    var inverted: Boolean

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
    fun apply(yaw: Float)

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
    fun rotatePitch(rads: Float) {
        relPitch = (relPitch + rads) % AnimationUtilities.FULL_ROTATION
    }

    /**
     * Add this number of degrees to the relative yaw
     */
    fun rotateYaw(rads: Float) {
        relYaw = (relYaw + rads) % AnimationUtilities.FULL_ROTATION
    }

    /**
     * Add this number of degrees to the relative roll
     */
    fun rotateRoll(rads: Float) {
        relRoll = (relRoll + rads) % AnimationUtilities.FULL_ROTATION
    }

    fun addOffset(offset: Vector3f) {
        //todo
    }

    fun update()
}