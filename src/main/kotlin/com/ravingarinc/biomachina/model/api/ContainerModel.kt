package com.ravingarinc.biomachina.model.api

import org.bukkit.World

open class ContainerModel(override var parent: Model? = null) : Model {
    private val children: MutableList<Model> = ArrayList()
    fun add(model: Model) {
        children.add(model)
    }

    fun remove(model: Model) {
        children.remove(model)
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        forEach {
            it.create(x, y, z, world)
        }
    }

    override fun destroy() {
        forEach { it.destroy() }
    }

    override fun update() {
        forEach { it.update() }
    }

    override fun forEach(consumer: (Model) -> Unit) {
        children.forEach {
            it.forEach(consumer)
        }
    }
}