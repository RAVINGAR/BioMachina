package com.ravingarinc.biomachina.vehicle.stat

class IntStat(private val min: Int, base: Int, private val max: Int) : Stat<Int>(base) {

    override fun modify(modifier: (Int) -> Int) {
        val mod = modifier.invoke(current.acquire)
        if(mod in min..max) {
            current.setRelease(mod)
        }
    }
}