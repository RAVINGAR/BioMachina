package com.ravingarinc.biomachina.model

import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

open class RootModel(val root: EntityModel) : EntityModel, ContainerModel() {
    override var entity: Entity?
        get() = root.entity
        set(value) {
            root.entity = value
        }

    override fun getEntityId(): Int {
        return root.getEntityId()
    }

    override fun getEntityUUID(): UUID {
        return root.getEntityUUID()
    }

    override fun create(x: Double, y: Double, z: Double, world: World) {
        super<ContainerModel>.create(x, y, z, world)
    }

    override fun spawn(x: Double, y: Double, z: Double, world: World): Entity? {
        throw IllegalStateException("Spawn should never be called on RootModel!")
    }

    override fun iterator(): Iterator<Model> {
        return object : Iterator<Model> {
            private var i = -1
            override fun hasNext(): Boolean {
                return i + 1 < children.size
            }

            override fun next(): Model {
                if(i == -1) {
                    i = 0
                    return root
                }
                return children[i++]
            }

        }
    }

    override fun forEach(consumer: (Model) -> Unit) {
        consumer.invoke(root)
        super.forEach(consumer)
    }
}

