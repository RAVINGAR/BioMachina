package com.ravingarinc.biomachina.viewer

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Group(override val uniqueId: UUID = UUID.randomUUID()) : MutableViewGroup {
    private val viewers: MutableMap<UUID, Viewer> = ConcurrentHashMap()
    private val parents: MutableSet<ViewGroup> = HashSet()
    override fun add(viewer: Viewer) : Boolean {
        if(viewers.put(viewer.uniqueId, viewer) == null) {
            viewer.apply { it.addParent(this) }
            return true
        }
        return false
    }

    override fun remove(viewer: Viewer) : Boolean {
        if(viewers.remove(viewer.uniqueId, viewer)) {
            viewer.apply { it.removeParent(this) }
            return true
        }
        return false
    }

    override fun size(): Int {
        return viewers.size
    }

    override fun addParent(parent: ViewGroup) {
        parents.add(parent)
    }

    override fun removeParent(parent: ViewGroup) {
        parents.remove(parent)
    }

    override fun destroy() {
        parents.forEach {
            if(it is MutableViewGroup) {
                it.remove(this)
            }
        }
    }

    override fun contains(viewer: Viewer): Boolean {
        return viewers.containsKey(viewer.uniqueId)
    }

    override fun apply(action: (Viewer) -> Unit) {
        viewers.values.forEach {
            it.apply(action)
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is Group) {
            return other.uniqueId == this.uniqueId
        }
        return false
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }

}