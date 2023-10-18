package com.ravingarinc.biomachina.model

import org.bukkit.World

open class ContainerModel : Model, Iterable<Model> {
    override var parent: Model? = null
    protected val children: MutableList<Model> = ArrayList()
    fun add(model: Model) {
        children.add(model)
    }

    fun remove(model: Model) {
        children.remove(model)
    }

    fun first() : Model? {
        if(children.isEmpty()) return null
        return children[0]
    }

    val size: Int get() = children.size

    override fun create(x: Double, y: Double, z: Double, world: World) {
        consumeEach { it.create(x, y, z, world) }
    }

    override fun destroy() {
        consumeEach { it.destroy() }
    }

    override fun consumeEach(consumer: (Model) -> Unit) {
        children.forEach {
            it.consumeEach(consumer)
        }
    }


    override fun iterator(): Iterator<Model> {
        return object : Iterator<Model> {
            private var i = 0
            override fun hasNext(): Boolean {
                return i < children.size
            }

            override fun next(): Model {
                return children[i++]
            }

        }
    }
}