package com.ravingarinc.biomachina.viewer

interface MutableViewGroup : ViewGroup {
    fun addViewer(viewer: Viewer)
    fun removeViewer(viewer: Viewer)
}
