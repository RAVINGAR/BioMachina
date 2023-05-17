package com.ravingarinc.biomachina.persistent

import com.ravingarinc.api.module.RavinPlugin
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf.StringTable

class VehicleDatabase(plugin: RavinPlugin) : SQLDatabase(VehicleDatabase::class.java, "vehicles", plugin) {
    object VehicleTable : UUIDTable("vehicles") {
        val type: Column<String> = varchar("type", 32)
        val x: Column<Double> = double("x")
        val y: Column<Double> = double("y")
        val z: Column<Double> = double("z")
        val worldName: Column<String> = varchar("world", 24)
        val upgrades: Column<String> = varchar("upgrades", 256)
    }
}