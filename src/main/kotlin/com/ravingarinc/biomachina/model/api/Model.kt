package com.ravingarinc.biomachina.model.api

import org.bukkit.World

interface Model {
    val parent: Model?

    /**
     * Spawn the entity representing this model
     */
    fun create(x: Double, y: Double, z: Double, world: World)

    /**
     * Remove the entity representing this model from the world
     */
    fun destroy()

    fun forEach(consumer: (Model) -> Unit)
}