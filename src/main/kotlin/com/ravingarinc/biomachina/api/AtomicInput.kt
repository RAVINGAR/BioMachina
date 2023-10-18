package com.ravingarinc.biomachina.api

import org.bukkit.util.Vector
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class AtomicInput {
    private val lastForwardsTime = AtomicLong(-1)
    private val lastSidewaysTime = AtomicLong(-1)
    private val forwards = AtomicInteger(0)
    private val sideways = AtomicInteger(0)

    val hasNoInput: Boolean get() {
        return forwards.get() == 0 && sideways.get() == 0
    }

    /**
     * Update this input with the current values
     */
    fun update(forwards: Float, sideways: Float) {
        if(forwards == 0F) {
            this.forwards.set(0)
            this.lastForwardsTime.set(-1)
        } else {
            if(forwards > 0F) {
                this.forwards.set(1)
            } else {
                this.forwards.set(-1)
            }
            if(this.lastForwardsTime.get() == -1L) {
                this.lastForwardsTime.set(System.currentTimeMillis())
            }

        }
        if(sideways == 0F) {
            this.sideways.set(0)
            lastSidewaysTime.set(-1)
        } else {
            if(sideways > 0F) {
                this.sideways.set(1)
            } else {
                this.sideways.set(-1)
            }
            if(this.lastSidewaysTime.get() == -1L) {
                this.lastSidewaysTime.set(System.currentTimeMillis())
            }
        }
    }

    /**
     * Represents a factor of movement based on how long a key press has been
     */
    fun motion() : Vector {
        val forwards = this.forwards.get()
        val sideways = this.sideways.get()
        val currentTime = System.currentTimeMillis()
        val lastForward = lastForwardsTime.get()
        val lastSideway = lastSidewaysTime.get()

        val sidewaysFactor = getTimeFactor(currentTime, lastSideway)
        if(forwards == 0) {
            return Vector(sideways * sidewaysFactor, 0F, 0F)
        }
        val forwardsFactor = getTimeFactor(currentTime, lastForward)
        if(sideways == 0) {
            return Vector(0F, 0F, forwards * forwardsFactor)
        }
        // Rudimentary normalisation
        return Vector(sideways * sidewaysFactor / 2F, 0F, forwards * forwardsFactor / 2F)
    }

    private fun getTimeFactor(currentTime: Long, lastTime: Long) : Float {
        if(lastTime == -1L) return 0F
        val factor = (currentTime - lastTime) / 2000F
        if(factor > 1F) return 1F
        return factor * factor
    }
}