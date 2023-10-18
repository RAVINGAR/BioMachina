package com.ravingarinc.biomachina.vehicle.stat

import com.ravingarinc.api.module.warn

open class FloatStat(private val min: Float, base: Float, private val max: Float) : Stat<Float>(base) {
    init {
        if(!(min..max).contains(base)) {
            warn("Float stat with base value '$base' is outside the specified minimum and maximum stat! Stat modifiers " +
                    "may not work because of this!")
        }
    }
    override fun modify(modifier: (Float) -> Float) {
        val mod = modifier.invoke(current.acquire)
        if(mod in min..max) {
            current.set(mod)
        }
    }
}