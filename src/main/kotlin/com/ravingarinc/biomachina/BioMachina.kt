package com.ravingarinc.biomachina

import com.ravingarinc.api.module.RavinPluginKotlin
import com.ravingarinc.biomachina.command.BioMachinaCommand
import com.ravingarinc.biomachina.persistent.PersistenceHandler
import com.ravingarinc.biomachina.persistent.VehicleDatabase
import com.ravingarinc.biomachina.protocol.AnimationHandler
import com.ravingarinc.biomachina.vehicle.VehicleListener
import com.ravingarinc.biomachina.vehicle.VehicleManager

class BioMachina : RavinPluginKotlin() {

    override fun loadModules() {
        addModule(PersistenceHandler::class.java)
        //addModule(VehicleDatabase::class.java)
        addModule(VehicleManager::class.java)
        addModule(AnimationHandler::class.java)

        addModule(VehicleListener::class.java)
    }
    override fun loadCommands() {
        BioMachinaCommand(this).register()
    }
}