package com.ravingarinc.biomachina.animation

import com.ravingarinc.biomachina.model.VehicleModel

abstract class Animation<T : VehicleModel>(val id: String) {
    /**
     * Perform this animation. Return true if this animation is done
     */
    abstract fun animate(controller: AnimationController<T>) : Boolean
}

abstract class RepeatingAnimation<T : VehicleModel>(id: String, private val period: Long, private val duration: Long) : Animation<T>(id) {
    private var iterations = 0L
    override fun animate(controller: AnimationController<T>) : Boolean {
        if(iterations < duration) {
            if(iterations++ % period == 0L) {
                tick(controller)
            }
            return false
        }
        return true
    }

    abstract fun tick(controller: AnimationController<T>)

}

abstract class PersistentAnimation<T : VehicleModel>(id: String, private val period: Long = 1) : Animation<T>(id) {
    private var isCancelled = false
    override fun animate(controller: AnimationController<T>): Boolean {
        if(isCancelled) {
            return true
        }
        tick(controller)
        return false
    }

    fun cancel() {
        isCancelled = true
    }

    abstract fun tick(controller: AnimationController<T>)
}

abstract class PersistentIntervalAnimation<T : VehicleModel>(id: String, private val period: Long) : Animation<T>(id) {
    private var isCancelled = false
    private var iteration = 0L
    override fun animate(controller: AnimationController<T>): Boolean {
        if(isCancelled) {
            return true
        }
        if(iteration++ % period == 0L) {
            tick(controller)
            iteration = 0
        }

        return false
    }

    fun cancel() {
        isCancelled = true
    }

    abstract fun tick(controller: AnimationController<T>)
}