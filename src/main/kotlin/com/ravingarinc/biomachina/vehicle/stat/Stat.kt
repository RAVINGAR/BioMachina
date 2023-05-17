package com.ravingarinc.biomachina.vehicle.stat

import java.util.concurrent.atomic.AtomicReference

open class Stat<T>(base: T) {
    protected val current = AtomicReference(base)
    open fun modify(modifier: (T) -> T) {
        current.setRelease(modifier.invoke(current.acquire))
    }
}