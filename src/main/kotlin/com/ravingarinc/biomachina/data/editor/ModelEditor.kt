package com.ravingarinc.biomachina.data.editor

import com.ravingarinc.api.gui.BaseGui
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Player

object ModelEditor {
    private val editors: MutableMap<VehicleType, BaseGui> = HashMap()
    fun openEditor(type: VehicleType, player: Player) {

    }

    fun applyChanges(type: VehicleType, player: Player) {

    }

    fun saveEditor(type: VehicleType) {

    }
}