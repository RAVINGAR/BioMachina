package com.ravingarinc.biomachina.vehicle.stat

import java.util.concurrent.atomic.AtomicReference

open class Stat<T>(val base: T) {
    protected val current = AtomicReference(base)
    open fun modify(modifier: (T) -> T) {
        current.set(modifier.invoke(current.acquire))
    }

    /**
     * Reset any modifiers and restore to base stat.
     */
    fun reset() {
        current.set(base)
    }

    fun value() : T {
        return current.acquire
    }
}