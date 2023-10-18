package com.ravingarinc.biomachina.model

import com.ravingarinc.biomachina.animation.AnimationUtilities
import org.joml.Quaternionf
import org.joml.Vector3f

interface VectorModel : Model {

    val origin: Vector3f
    val leftRotation: Quaternionf
    val rightRotation: Quaternionf
    val scale: Vector3f

    val rotatingOrigin: Vector3f

    var inverted: Boolean

    /**
     * Pitch rotates on the x-axis in degrees
     */
    var absPitch: Float
    /**
     * Yaw rotates on the y-axis in degrees
     */
    var absYaw: Float

    /**
     * Roll rotates on the z-axis in degrees
     */
    var absRoll: Float

    /**
     * Reapply the transformation
     */
    fun apply(yaw: Float, pitch: Float, roll: Float)

    /**
     * Pitch rotates on the x-axis in radians. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relPitch: Float

    /**
     * Yaw rotates on the y-axis in radians. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relYaw: Float

    /**
     * Roll rotates on the z-axis in radians. This is always relative for the specific model
     * and should never consider the parent rotation.
     */
    var relRoll: Float

    /**
     * Add this number of radians to the relative pitch.
     */
    fun rotatePitch(rads: Float) {
        if(rads == 0F) return
        relPitch = (relPitch + rads) % AnimationUtilities.FULL_ROTATION_RADS
    }

    /**
     * Add this number of radians to the relative yaw
     */
    fun rotateYaw(rads: Float) {
        if(rads == 0F) return
        relYaw = (relYaw + rads) % AnimationUtilities.FULL_ROTATION_RADS
    }

    /**
     * Add this number of radians to the relative roll
     */
    fun rotateRoll(rads: Float) {
        if(rads == 0F) return
        relRoll = (relRoll + rads) % AnimationUtilities.FULL_ROTATION_RADS
    }

    fun applyRelativeRotations()

    fun applyAbsoluteRotations()
}