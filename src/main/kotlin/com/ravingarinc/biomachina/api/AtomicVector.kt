package com.ravingarinc.biomachina.api

import com.google.common.util.concurrent.AtomicDouble
import org.bukkit.util.Vector

class AtomicVector(x: Double = 0.0, y: Double = 0.0, z : Double = 0.0) : Cloneable {
    private val x = AtomicDouble(x)
    private val y = AtomicDouble(y)
    private val z = AtomicDouble(z)

    fun add(vec: Vector) : AtomicVector {
        x.addAndGet(vec.x)
        y.addAndGet(vec.y)
        z.addAndGet(vec.z)
        return this
    }

    fun subtract(vec: Vector) : AtomicVector {
        x.addAndGet(-vec.x)
        y.addAndGet(-vec.y)
        z.addAndGet(-vec.z)
        return this
    }

    fun multiply(vec: Vector) : AtomicVector {
        x.set(x.get() * vec.x)
        y.set(y.get() * vec.y)
        z.set(z.get() * vec.z)
        return this
    }

    fun divide(vec: Vector) : AtomicVector {
        x.set(x.get() / vec.x)
        y.set(y.get() / vec.y)
        z.set(z.get() / vec.z)
        return this
    }

    override fun clone(): Vector {
        return Vector(x.get(), y.get(), z.get())
    }
}