package com.ravingarinc.biomachina.command

import com.github.shynixn.mccoroutine.bukkit.launch
import com.ravingarinc.api.command.BaseCommand
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.chat.callback
import com.ravingarinc.biomachina.api.round
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
            manager.createVehicle(type, Location(world, x.round(2), y.round(2), z.round(2)))
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

        addOption("editor", null, "- Edit a vehicle type's transformations", 2) { sender, args ->
            val type = Vehicle.Factory.getType(args[1])
            if(type == null) {
                sender.sendRichMessage("<red>Could not find vehicle type called '${args[1]}'")
                return@addOption true
            }
            if(!(sender is Player)) {
                sender.sendRichMessage("<red>Only a player can use this command!")
                return@addOption true
            }
            if(manager.hasEditorSession(sender)) {
                sender.sendRichMessage("<red>You already have an open editor session!")
                sender.sendMessage(Component.text()
                    .content("Would you like to discard your previous session and start a new one?")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text()
                        .content("\n[Discard and Create]")
                        .color(NamedTextColor.RED)
                        .callback(Component.text("Discard previous editor.").color(NamedTextColor.GRAY)) {
                            if (manager.hasEditorSession(it)) manager.discardEditorSession(it)
                            manager.openEditorSession(it, type)
                        }))
            } else {
                manager.openEditorSession(sender, type)
            }
            return@addOption true
        }.buildTabCompletions { _, args ->
            if(args.size == 2) {
                return@buildTabCompletions ArrayList(Vehicle.Factory.getTypes())
            }

            return@buildTabCompletions emptyList<String>()
        }

        addHelpOption(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE)
    }


}