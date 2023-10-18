package com.ravingarinc.biomachina.persistent

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.SingletonHolder

class Properties private constructor(plugin: RavinPlugin) {
    private val manager: PersistenceHandler = plugin.getModule(PersistenceHandler::class.java)

    // database properties //
    /*
    val world: Property<World> by lazy {
        Property(
            "world", manager.read(
                "defaults",
                "world-name",
                ConfigurationSection::getString,
                { plugin.server.getWorld(it) },
                plugin.server.worlds[0]
            )
        ) { plugin.server.getWorld(it) ?: throw IllegalArgumentException("Could not find world called $it") }
    }*/


    val minSpeed: Property<Float> = Property(
        manager.read(
            "stats.speed.min",
            0F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val maxSpeed: Property<Float> = Property(
        manager.read(
            "stats.speed.max",
            1F) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val minAccel: Property<Float> = Property(
        manager.read(
            "stats.acceleration.min",
            0F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val maxAccel: Property<Float> = Property(
        manager.read(
            "stats.acceleration.max",
            1F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val minTerrainHeight: Property<Float> = Property(
        manager.read(
            "stats.terrain_height.min",
            0F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val maxTerrainHeight: Property<Float> = Property(
        manager.read(
            "stats.terrain_height.max",
            5F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val minBrakingPower: Property<Float> = Property(
        manager.read(
            "stats.braking_power.min",
            1.0F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    val maxBrakingPower: Property<Float> = Property(
        manager.read(
            "stats.braking_power.max",
            5.0F
        ) {
            return@read this.getDouble(it).toFloat()
        }
    ) { it.toFloatOrNull() ?: throw IllegalArgumentException("Could not parse $it as a float!") }

    fun <T> set(property: Property<T>, value: T) {
        property.value = value
    }

    companion object : SingletonHolder<Properties, RavinPlugin>(::Properties)

    data class Property<T>(var value: T, private val transformer: (String) -> T) {
        fun set(string: String) {
            value = transformer.invoke(string)
        }
    }
}