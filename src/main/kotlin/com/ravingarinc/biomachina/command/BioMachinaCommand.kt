package com.ravingarinc.biomachina.command

import com.github.shynixn.mccoroutine.bukkit.launch
import com.ravingarinc.api.command.BaseCommand
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.data.editor.EditorSession
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import kotlinx.coroutines.Dispatchers
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Particle
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

        addOption("test", null, "Test command", 1) { sender, _ ->
            if(sender is Player) {
                val loc = sender.location
                plugin.launch(Dispatchers.IO) {
                    sender.spawnParticle(Particle.VILLAGER_HAPPY, loc, 10)
                    sender.sendMessage("Success!")
                }
            }

            return@addOption true
        }

        addOption("reload", null, "Reload this plugin", 1) { sender, _ ->
            plugin.reload()
            sender.sendMessage("<green>Successfully reloaded BioMachina!")
            return@addOption true
        }

        addOption("summon", null, "<type> <x> <y> <z> <world> - Summon a new vehicle", 5) { sender, args ->
            val type = Vehicle.Factory.getType(args[1])
            if(type == null) {
                sender.sendRichMessage("<red>Could not find vehicle type called '${args[1]}'")
                return@addOption true
            }
            val x = args[2].toDoubleOrNull()
            val y = args[3].toDoubleOrNull()
            val z = args[4].toDoubleOrNull()
            if(x == null || y == null || z == null) {
                sender.sendRichMessage("<red>Could not parse coordinates for vehicle summon!")
                return@addOption true
            }
            val world = if(args.size > 5) plugin.server.getWorld(args[5]) else if(sender is Player) sender.world else null
            if(world == null) {
                sender.sendRichMessage("<red>Could not find world for vehicle summon!")
                return@addOption true
            }
            manager.createVehicle(type, Location(world, x, y + 1, z))
            sender.sendRichMessage("<green>Successfully summoned vehicle!")
            return@addOption true
        }.buildTabCompletions { sender, args ->
            if(args.size == 2) {
                return@buildTabCompletions ArrayList(Vehicle.Factory.getTypes())
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

        addOption("remove", null, "- Remove an existing vehicle", 1) { sender, args ->
            TODO()
        }

        addOption("model", null, "- Edit a vehicle type's transformations", 2) { sender, args ->
            val type = Vehicle.Factory.getType(args[1])
            if(type == null) {
                sender.sendRichMessage("<red>Could not find vehicle type called '${args[1]}'")
                return@addOption true
            }
            if(!(sender is Player)) {
                sender.sendRichMessage("<red>Only a player can use this command!")
                return@addOption true
            }


            //todo save original vehicle type stats here

            //todo add a thing which interrupts chat whilst editing here!

            val session = EditorSession(plugin, type, sender)
            session.open()

            return@addOption true
        }

        addHelpOption(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE)
    }


}