package com.ravingarinc.biomachina.persistent

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.SingletonHolder
import org.bukkit.configuration.ConfigurationSection

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
    /*

    val minSpeed: Property<Int> = Property(
        manager.read(
            "stats",
            "speed.min",
            0,
            ConfigurationSection::getInt
        )
    ) { it.toIntOrNull() ?: throw IllegalArgumentException("Could not parse $it as a double!") }

    val maxSpeed: Property<Int> = Property(
        manager.read(
            "stats",
            "speed.max",
            1,
            ConfigurationSection::getInt
        )
    ) { it.toIntOrNull() ?: throw IllegalArgumentException("Could not parse $it as a double!") }

    val minAccel: Property<Int> = Property(
        manager.read(
            "stats",
            "acceleration.min",
            0,
            ConfigurationSection::getInt
        )
    ) { it.toIntOrNull() ?: throw IllegalArgumentException("Could not parse $it as a double!") }

    val maxAccel: Property<Int> = Property(
        manager.read(
            "stats",
            "acceleration.max",
            1,
            ConfigurationSection::getInt
        )
    ) { it.toIntOrNull() ?: throw IllegalArgumentException("Could not parse $it as a double!") }

     */

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