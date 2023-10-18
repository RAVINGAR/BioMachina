package com.ravingarinc.biomachina.vehicle.stat

import com.ravingarinc.api.module.warn

class IntStat(private val min: Int, base: Int, private val max: Int) : Stat<Int>(base) {
    init {
        if(!(min..max).contains(base)) {
            warn("Integer stat with base value '$base' is outside the specified minimum and maximum stat! Stat modifiers " +
                    "may not work because of this!")
        }
    }
    override fun modify(modifier: (Int) -> Int) {
        val mod = modifier.invoke(current.acquire)
        if(mod in min..max) {
            current.set(mod)
        }
    }
}