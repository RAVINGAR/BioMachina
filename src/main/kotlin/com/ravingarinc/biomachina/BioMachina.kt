package com.ravingarinc.biomachina

import com.ravingarinc.api.I
import com.ravingarinc.api.module.RavinPluginKotlin
import com.ravingarinc.biomachina.animation.AnimationHandler
import com.ravingarinc.biomachina.api.Versions
import com.ravingarinc.biomachina.command.BioMachinaCommand
import com.ravingarinc.biomachina.persistent.PersistenceHandler
import com.ravingarinc.biomachina.vehicle.VehicleListener
import com.ravingarinc.biomachina.vehicle.VehicleManager
import java.util.logging.Level

class BioMachina : RavinPluginKotlin() {

    override fun onEnable() {
        super.onEnable()
        try {
            Class.forName("com.destroystokyo.paper.Namespaced")
            Versions.validateVersion()
        } catch(exception: ClassNotFoundException) {
            I.log(Level.SEVERE, "BioMachina requires Paper or a variant of to load! This plugin will now be disabled!")
            onDisable()
        } catch(exception: IllegalStateException) {
            I.log(Level.SEVERE, "BioMachina encountered an exception whilst enabling!", exception)
            onDisable()
        }
    }
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