package com.ravingarinc.biomachina.viewer

interface MutableViewGroup : ViewGroup {
    fun add(viewer: Viewer) : Boolean
    fun remove(viewer: Viewer) : Boolean
}
