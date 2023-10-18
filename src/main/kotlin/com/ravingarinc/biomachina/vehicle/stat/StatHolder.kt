package com.ravingarinc.biomachina.vehicle.stat

import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.persistent.Properties

class StatHolder {
    val stats: MutableMap<String, Stat<*>> = HashMap()

    fun <T, U : Stat<T>> registerStat(properties: Properties, stat: StatProvider<T, U>, base: T) {
        val identifier = stat.identifier
        if(stats.containsKey(identifier)) {
            warn("Could not register stat with identifier $identifier twice! This is a developer error!")
            return
        }
        stats[identifier] = stat.create(properties, base)
    }

    inline fun <reified T, U : Stat<T>> getStat(stat: StatProvider<T, U>) : Stat<T> {
        val statInstance = this.stats[stat.identifier] ?: throw IllegalArgumentException("Could not find stat '${stat.identifier}' in this stat holder!")
        if(statInstance.base is T) {
            return (statInstance as Stat<T>)
        }
        throw IllegalStateException("Encountered mismatched stat type parameters for stat '${stat.identifier}'")
    }

    fun resetStat(stat: StatProvider<*, *>) {
        val statInstance = this.stats[stat.identifier] ?: return
        statInstance.reset()
    }
}