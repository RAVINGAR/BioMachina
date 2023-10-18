package com.ravingarinc.biomachina.persistent

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModule
import com.ravingarinc.api.module.warn
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class PersistenceHandler(plugin: RavinPlugin) : SuspendingModule(PersistenceHandler::class.java, plugin) {
    private val config: ConfigFile = ConfigFile(plugin, "config.yml")
    private val vehicleConfigs: MutableList<ConfigurationSection> = ArrayList()

    override suspend fun suspendLoad() {
        val vehicles = File(plugin.dataFolder, "vehicles")
        if(!vehicles.exists()) {
            vehicles.mkdirs()
            copyResource(plugin, vehicles, "vehicle_example.yml", "vehicle_example.yml")
        }
        val resources = File(plugin.dataFolder, "resources")
        if (!resources.exists()) {
            resources.mkdirs()
        }
        val json = File(plugin.dataFolder, "json")
        if (!json.exists()) {
            json.mkdirs()
        }
        for(f in vehicles.listFiles()!!) {
            if(f.isFile && f.name.endsWith(".yml")) {
                val configuration = YamlConfiguration.loadConfiguration(f)
                configuration.getKeys(false).forEach { key ->
                    vehicleConfigs.add(configuration.getConfigurationSection(key)!!)
                }
            }
        }
        Properties.getInstance(plugin)
        // Todo need to add a thing here to basically reload all the config values
    }

    fun getVehicleConfigs() : List<ConfigurationSection> {
        return vehicleConfigs
    }

    override suspend fun suspendCancel() {
        config.reload()
        vehicleConfigs.clear()
    }

    fun <T> read(path: String, default: T, section: ConfigurationSection.(String) -> T?): T {
        var readValue = default
        section.invoke(config.config, path)?.let {
            readValue = it
        } ?: warn("Could not find configuration option at path '$path' in config.yml!")
        return readValue
    }

    fun copyResource(plugin: RavinPlugin, parent: File, sourcePath: String, destPath: String) {
        plugin.getResource(sourcePath)?.let {
            it.use { stream ->
                Files.copy(
                    stream,
                    File(parent, destPath).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }
    }
}
