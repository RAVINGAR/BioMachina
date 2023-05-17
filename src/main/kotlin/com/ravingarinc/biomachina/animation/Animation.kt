package com.ravingarinc.biomachina.animation

import com.ravingarinc.biomachina.model.api.EntityModel
import com.ravingarinc.biomachina.model.api.Model
import kotlinx.coroutines.CoroutineScope

abstract class Animation<T : EntityModel>(val id: String) {
    /**
     * Perform this animation. Return true if this animation is done
     */
    abstract fun animate(controller: AnimationController<T>) : Boolean
}

abstract class RepeatingAnimation<T : EntityModel>(id: String, private val period: Long, private val duration: Long) : Animation<T>(id) {
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

abstract class PersistentAnimation<T : EntityModel>(id: String, private val period: Long) : Animation<T>(id) {
    private var isCancelled = false
    private var iterations = 1L;
    override fun animate(controller: AnimationController<T>): Boolean {
        if(isCancelled) {
            return true
        }
        //period of 3, so it goes 1, 2, 3
        if(iterations++ % period == 0L) {
            iterations = 1L
            tick(controller)
        }
        return false
    }

    fun cancel() {
        isCancelled = true
    }

    abstract fun tick(controller: AnimationController<T>)
}