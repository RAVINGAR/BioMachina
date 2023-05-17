package com.ravingarinc.biomachina.api

import com.ravingarinc.api.module.Module
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.warn
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import kotlin.random.Random


fun <T : Module> RavinPlugin.withModule(module: Class<T>, function: T.() -> Unit) {
    val m = this.getModule(module)
    if (m.isLoaded) {
        function.invoke(m)
    } else {
        warn("Could not execute function with module ${module.name} as this module has not been loaded!")
    }
}

fun formatMilliseconds(milliseconds: Long): String {
    if (milliseconds == -1L) {
        return "Nil"
    }
    return if (milliseconds > 1000) {
        "${milliseconds / 1000.0F} s"
    } else {
        "$milliseconds ms"
    }
}

fun Double.roll(): Boolean {
    return this > 0.0 && (this >= 1.0 || Random.nextDouble() < this)
}

fun ConfigurationSection.getPercentage(path: String): Double {
    if (!this.contains(path)) {
        warn("Could not find option at $path in ${this.name}!")
        return 0.0
    }
    return parsePercentage(getString(path)?.replace("%", ""))
}

fun parsePercentage(string: String?): Double {
    if (string == null) {
        warn("Could not parse $string as a percentage! Format must be 0.4 or 40%!")
        return 0.0
    }
    var double = string.replace("%", "").toDoubleOrNull()
    if (double == null) {
        warn("Could not parse $string as a percentage! Format must be 0.4 or 40%!")
        return 0.0
    }
    if (double > 1.0) {
        double /= 100.0
    }
    return double
}

fun ConfigurationSection.getRange(path: String): IntRange {
    if (!this.contains(path)) {
        warn("Could not find option at $path in ${this.name}!")
        return IntRange(0, 0)
    }
    return parseRange(getString(path)!!.replace(" ", ""))
}

fun parseRange(string: String): IntRange {
    val split: List<String> = if (string.contains("-")) string.split("-") else string.split("to")
    if (split.size == 2) {
        val min = split[0].toIntOrNull()
        val max = split[1].toIntOrNull()
        if (min == null) {
            warn("Could not parse minimum value of '${split[0]}' as a valid number!")
            return IntRange(0, 0)
        }
        if (max == null) {
            warn("Could not parse maximum value of '${split[1]}' as a valid number!")
            return IntRange(0, 0)
        }
        return IntRange(min, max)
    }
    warn("Could not parse $string as a valid range! Please use the format '3-4' or '3to4'")
    return IntRange(0, 0)
}

fun ConfigurationSection.getMaterialList(path: String): Set<Material> {
    val list = getStringList(path)
    return buildSet {
        for (m in list) {
            parseMaterial(m)?.let { this.add(it) }
        }
    }
}

fun ConfigurationSection.getMaterial(path: String): Material? {
    val material = this.getString(path)
    if (material == null) {
        warn("Could not find option at path '$path' in section '${this.name}'")
        return null
    }
    return parseMaterial(material)
}

fun ConfigurationSection.getSound(path: String): Sound? {
    getString(path)?.let {
        try {
            return Sound.valueOf(it.uppercase())
        } catch (exception: IllegalArgumentException) {
            warn("Could not find sound with ID of '$it'!")
        }
    }
    warn("Could not find option at path '$path' in section '${this.name}'")
    return null
}

fun parseMaterial(string: String): Material? {
    val material = Material.matchMaterial(string)
    if (material == null) {
        warn("Could not find valid material called '$string'. Please fix your config!")
    }
    return material
}

