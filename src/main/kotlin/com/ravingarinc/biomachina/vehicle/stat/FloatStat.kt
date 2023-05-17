package com.ravingarinc.biomachina.vehicle.stat

open class FloatStat(private val min: Float, base: Float, private val max: Float) : Stat<Float>(base) {

    override fun modify(modifier: (Float) -> Float) {
        val mod = modifier.invoke(current.acquire)
        if(mod in min..max) {
            current.setRelease(mod)
        }
    }
}