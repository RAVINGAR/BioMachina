package com.ravingarinc.biomachina.data

import com.ravingarinc.biomachina.animation.rotate
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.max
import kotlin.math.min

class CollisionBox(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
    protected val minX = min(x1, x2)
    protected val minY = min(y1, y2)
    protected val minZ = min(z1, z2)

    protected val maxX = max(x1, x2) // Collision Box has these as +1 for some reason?
    protected val maxY = max(y1, y2)
    protected val maxZ = max(z1, z2)

    val min: Vector get() = Vector(minX, minY, minZ)

    val max: Vector get() = Vector(maxX, maxY, maxZ)

    private var lastYaw: Float = 0F
    private var lastPitch: Float = 0F
    private var lastRoll: Float = 0F

    private var lastFront: Vector? = null
    private var lastBottom: Vector? = null

    val absoluteCentre: Vector by lazy {
        Vector((minX + maxX) / 2F, (minY + maxY) / 2F, (minZ + maxZ) / 2F)
    }

    val height: Float get() = maxY - minY

    /**
     * Returns a vector representing the front and top of the collision box
     */
    fun getRelativeFront(yaw: Float, pitch: Float, roll: Float) : Vector {
        lastFront.let {
            if(it == null || yaw != lastYaw || pitch != lastPitch || roll != lastRoll) {
                lastYaw = yaw
                lastPitch = pitch
                lastRoll = roll

                // yaw is 0 when facing south (positive z)
                // If the front plane is represented by highest x and z. Then facing
                // with -90 yaw is considered Forward (so we must rotate from that)


                val vector = Vector((maxX + minX) / 2F, maxY, maxZ)
                    .rotate(Location.normalizeYaw(yaw - 90F), pitch)
                lastFront = vector
                return vector
            }
        }
        return lastFront!! // If this throws here this is a concurrency issue
    }

    fun getCentreBottom(yaw: Float, pitch: Float, roll: Float) : Vector {
        lastBottom.let {
            if(it == null || yaw != lastYaw || pitch != lastPitch || roll != lastRoll) {
                lastYaw = yaw
                lastPitch = pitch
                lastRoll = roll

                val vector = Vector((minX + maxX) / 2F, minY, (minZ + maxZ) / 2F)
                    .rotate(yaw, pitch)
                lastBottom = vector
                return vector
            }
        }
        return lastBottom!!
    }
}