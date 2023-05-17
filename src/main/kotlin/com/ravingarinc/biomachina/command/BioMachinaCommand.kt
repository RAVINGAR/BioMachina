package com.ravingarinc.biomachina.command

import com.ravingarinc.api.command.BaseCommand
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.vehicle.MotorVehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.BiFunction

class BioMachinaCommand(plugin: RavinPlugin) : BaseCommand(
    plugin,
    "biomachina",
    "biomachina.admin",
    "Master command",
    0,
    BiFunction { _, _ ->
        return@BiFunction false
    }) {
    init {
        val manager = plugin.getModule(VehicleManager::class.java)!!

        addOption("reload", null, "Reload this plugin", 1) { sender, _ ->
            plugin.reload()
            sender.sendMessage("${ChatColor.GREEN}Successfully reloaded BioMachina!")
            return@addOption true
        }

        addOption("summon", null, "Summon a new vehicle", 5) { sender, args ->
            val type = MotorVehicle.Factory.getType(args[1])
            if(type == null) {
                sender.sendMessage("${ChatColor.RED}Could not find vehicle type called '${args[1]}'")
                return@addOption true
            }
            val x = args[2].toDoubleOrNull()
            val y = args[3].toDoubleOrNull()
            val z = args[4].toDoubleOrNull()
            if(x == null || y == null || z == null) {
                sender.sendMessage("${ChatColor.RED}Could not parse coordinates for vehicle summon!")
                return@addOption true
            }
            val world = if(args.size > 5) plugin.server.getWorld(args[5]) else if(sender is Player) sender.world else null
            if(world == null) {
                sender.sendMessage("${ChatColor.RED}Could not find world for vehicle summon!")
                return@addOption true
            }
            manager.createVehicle(type, Location(world, x, y + 1, z))
            sender.sendMessage("${ChatColor.GREEN}Successfully summoned vehicle!")
            return@addOption true
        }.buildTabCompletions { sender, args ->
            if(args.size == 2) {
                return@buildTabCompletions ArrayList(MotorVehicle.Factory.getTypes())
            }
            else {
                if(sender is Player) {
                    val list : MutableList<String> = ArrayList()
                    val loc = sender.getTargetBlockExact(4)?.location ?: sender.location

                    when (args.size) {
                        3 -> { list.add(loc.x.toString()) }
                        4 -> { list.add(loc.y.toString()) }
                        5 -> { list.add(loc.z.toString())}
                    }
                    return@buildTabCompletions list
                }
            }

            return@buildTabCompletions emptyList<String>()
        }

        addOption("remove", null, "Remove an existing vehicle", 1) { sender, args ->
            //todo
            return@addOption true
        }
    }
}