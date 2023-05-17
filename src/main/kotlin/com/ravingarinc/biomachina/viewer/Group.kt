package com.ravingarinc.biomachina.viewer

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

class Group(override val uniqueId: UUID = UUID.randomUUID()) : MutableViewGroup {
    private val viewers: MutableMap<UUID, Viewer> = ConcurrentHashMap()
    private val parents: MutableSet<ViewGroup> = HashSet()
    override fun addViewer(viewer: Viewer) {
        viewer.consume {
            if(viewers.put(it.uniqueId, it) == null) {
                it.addParent(this)
            }
        }
    }

    override fun removeViewer(viewer: Viewer) {
        viewer.consume {
            if(viewers.remove(it.uniqueId, it)) {
                it.removeParent(this)
            }
        }
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
                it.removeViewer(this)
            }
        }
    }

    override fun contains(viewer: Viewer): Boolean {
        return viewers.containsKey(viewer.uniqueId)
    }

    override fun consume(action: (Viewer) -> Unit) {
        viewers.forEach {
            it.value.consume(action)
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