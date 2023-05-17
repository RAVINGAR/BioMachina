package com.ravingarinc.biomachina.model.api

import org.bukkit.World

interface Model {
    var parent: Model?

    /**
     * Spawn the entity representing this model
     */
    fun create(x: Double, y: Double, z: Double, world: World)

    /**
     * Remove the entity representing this model from the world
     */
    fun destroy()

    /**
     * Updates this model and re-syncs any changes from the parent model data
     */
    fun update()

    fun forEach(consumer: (Model) -> Unit)
}