package com.ravingarinc.biomachina.data.editor

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.chat.callback
import com.ravingarinc.biomachina.data.ModelTransformation
import com.ravingarinc.biomachina.data.copy
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import com.ravingarinc.biomachina.vehicle.VehiclePart
import com.ravingarinc.biomachina.vehicle.VehicleType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.*

class EditorSession(private val plugin: RavinPlugin, private val type: VehicleType, private val player: Player) {
    private val manager = plugin.getModule(VehicleManager::class.java)
    private val vehicle: Vehicle
    private val editHistory: Deque<Edit<*>> = LinkedList() // create a cycling queue
    private var interval: Float = 0.25F
    private var degrees: Float = 15F
    private var isDisposed = false


    private val componentProviders: Map<VehiclePart.Type, Component> = buildMap {
        type.allParts().forEach {
            val builder = Component.text()
            for(i in it.value.indices) {
                builder.append(getPartMessage("${it.key.display} #${i+1}", it.value[i]))
                builder.append(Component.text("\n"))
            }
            this[it.key] = builder.build()
        }
    }

    init {
        val loc = player.eyeLocation
        loc.add(loc.direction.normalize().multiply(3))
        vehicle = manager.createVehicle(type, loc)
    }

    fun open() {
        player.sendRichMessage("<yellow>Editor session has been opened!\n")
        send(VehiclePart.Type.CHASSIS)
    }

    private fun send(part: VehiclePart.Type) {
        player.sendRichMessage("<gray>-------- <bold><dark_purple>Vehicle Model Editor</dark_purple></bold> --------")
        player.sendRichMessage("<gray>Vehicle Type - '<light_purple>${type.identifier}'")
        player.sendRichMessage("<gray>Please note your changes will not be applied unless saved!\n")

        player.sendMessage(Component.text()
            .content("<Set Interval>")
            .color(NamedTextColor.GRAY)
            .callback("Click to view current values") {
                it.sendActionBar(Component.text("Current Interval = $interval"))
            }
            .append(Component.text(" - "))
            .append(
                Component.text()
                    .content("[0.01 | 1°]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .callback("Set interval to 0.01 and degrees to 1°") { setInterval(0.01F, 1F) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[0.10 | 5°]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .callback("Set interval to 0.10 and degrees to 5°") { setInterval(0.10F, 5F) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[0.25 | 15°]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .callback("Set interval to 0.25 and degrees to 15°") { setInterval(0.25F, 15F) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[0.50 | 45°]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .callback("Set interval to 0.50 and degrees to 45°") { setInterval(0.5F, 45F) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[1.0 | 90°]")
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .callback("Set interval to 1.0 and degrees to 90°") { setInterval(1F, 90F) }
            )
            .append(Component.text("\n"))
            .build())
        componentProviders[part]?.let {
            player.sendMessage(it)
        }

        val builder = Component.text().content("Parts: ").color(NamedTextColor.GRAY)
        type.allParts().keys.filter { it != part }.forEach {
            builder.append(Component.text()
                .content("[${it.display}]")
                .color(NamedTextColor.LIGHT_PURPLE)
                .callback("Edit this part model.") { _ ->
                    player.sendMessage(Component.text(""))
                    player.sendMessage(Component.text(""))
                    player.sendMessage(Component.text(""))
                    player.sendMessage(Component.text(""))
                    send(it)
                })
            builder.append(Component.text(" "))
        }
        player.sendMessage(builder.build())

        player.sendMessage(Component.text()
            .append(Component.text()
                .content("[Save]")
                .color(NamedTextColor.GREEN)
                .callback("Save all changes and apply to all models") {
                    save()
                })
            .append(Component.text(" "))
            .append(Component.text()
                .content("[Discard]")
                .color(NamedTextColor.RED)
                .callback("Discard all changes and revert to previous load") {
                    discard()
                }).build())
    }

    private fun setInterval(newInterval: Float, newDegrees: Float) {
        this.interval = newInterval
        this.degrees = newDegrees
        player.sendActionBar(Component.text("Interval: ${interval}, Degrees: ${newDegrees}°").color(NamedTextColor.YELLOW))
    }

    private fun getPartMessage(name: String, data: VehiclePart) : TextComponent.Builder {
        val model = data.model
        return (Component.text()
            .content("-- $name --\n")
            .color(NamedTextColor.DARK_PURPLE)
            .append(createVectorCallback("Origin", model.origin))
            .append(createRotationCallback("Rotation", model))
            .append(createVectorCallback("Scale", model.scale)))
    }

    private fun createVectorCallback(name: String, vector: Vector3f) : ComponentLike {
        return Component.text()
            .content("<$name>")
            .color(NamedTextColor.GRAY)
            .callback("Click to view current values") {
                it.sendActionBar(Component.text("$name = [x = ${vector.x}, y = ${vector.y}, z = ${vector.z}").color(NamedTextColor.GRAY))
            }
            .append(Component.text(" - "))
            .append(
                Component.text()
                    .content("[+x]")
                    .color(NamedTextColor.GREEN)
                    .callback("Increase $name's x axis") { edit(vector, VectorEdit.Type.X, interval) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[+y]")
                    .color(NamedTextColor.GREEN)
                    .callback("Increase $name's y axis") { edit(vector, VectorEdit.Type.Y, interval) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[+z]")
                    .color(NamedTextColor.GREEN)
                    .callback("Increase $name's z axis") { edit(vector, VectorEdit.Type.Z, interval) }
            )
            .append(Component.text(" | "))
            .append(
                Component.text()
                    .content("[-x]")
                    .color(NamedTextColor.RED)
                    .callback("Decrease $name's x axis") { edit(vector, VectorEdit.Type.X, -interval) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[-y]")
                    .color(NamedTextColor.RED)
                    .callback("Decrease $name's y axis") { edit(vector, VectorEdit.Type.Y, -interval) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[-z]")
                    .color(NamedTextColor.RED)
                    .callback("Decrease $name's z axis") { edit(vector, VectorEdit.Type.Z, -interval) }
            )
            .append(Component.text("\n"))
    }

    private fun createRotationCallback(name: String, rotation: ModelTransformation) : ComponentLike {
        return Component.text()
            .content("<$name>")
            .color(NamedTextColor.GRAY)
            .callback("Click to view current values") {
                it.sendActionBar(Component.text("$name = [yaw = ${rotation.yaw}, pitch = ${rotation.pitch}, roll = ${rotation.roll}]]").color(NamedTextColor.GRAY))
            }
            .append(Component.text(" - "))
            .append(
                Component.text()
                    .content("[+yaw]")
                    .color(NamedTextColor.GREEN)
                    .callback("Rotate $name's x axis") { edit(rotation, RotationEdit.Type.YAW, degrees) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[+pitch]")
                    .color(NamedTextColor.GREEN)
                    .callback("Rotate $name's y axis") { edit(rotation, RotationEdit.Type.PITCH, degrees) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[+roll]")
                    .color(NamedTextColor.GREEN)
                    .callback("Rotate $name's z axis") { edit(rotation, RotationEdit.Type.ROLL, degrees) }
            )
            .append(Component.text(" | "))
            .append(
                Component.text()
                    .content("[-yaw]")
                    .color(NamedTextColor.RED)
                    .callback("Rotate $name's x axis") { edit(rotation, RotationEdit.Type.YAW, -degrees) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[-pitch]")
                    .color(NamedTextColor.RED)
                    .callback("Rotate $name's y axis") { edit(rotation, RotationEdit.Type.PITCH, -degrees) }
            )
            .append(Component.text(" "))
            .append(
                Component.text()
                    .content("[-roll]")
                    .color(NamedTextColor.RED)
                    .callback("Rotate $name's z axis") { edit(rotation, RotationEdit.Type.ROLL, -degrees) }
            )
            .append(Component.text("\n"))
    }

    private fun save() {
        type.save(manager)
        player.sendRichMessage("<yellow>Changes have been saved!")
        this.close()
    }

    private fun discard() {
        type.reload(manager)
        player.sendRichMessage("<yellow>Changes have been discarded!")
        this.close()
    }

    private fun close() {
        manager.removeVehicle(vehicle)
        manager.getVehicles().filter { it.type == this.type }.forEach { it.update() }
        player.sendRichMessage("<yellow>Editor session has been closed!")
        isDisposed = true
    }


    private fun edit(vector: Vector3f, type: VectorEdit.Type, interval: Float) {
        if(isDisposed) {
            player.sendRichMessage("<red>You cannot perform this edit as the editor session has been closed!")
            return
        }
        val edit = VectorEdit()
        edit.apply(vector) {
            when(type) {
                VectorEdit.Type.X -> it.add(interval, 0F, 0F)
                VectorEdit.Type.Y -> it.add(0F, interval, 0F)
                VectorEdit.Type.Z -> it.add(0F, 0F, interval)
            }
        }
        edit(edit)
        player.sendActionBar(Component.text("Vector = [x = ${vector.x}, y = ${vector.y}, z = ${vector.z}]").color(NamedTextColor.YELLOW))
    }

    private fun edit(rotation: ModelTransformation, type: RotationEdit.Type, degrees: Float) {
        if(isDisposed) {
            player.sendRichMessage("<red>You cannot perform this edit as the editor session has been closed!")
            return
        }
        val edit = RotationEdit()
        //todo this should actually be based on a rotation, and the math is done here!
        // todo check what the difference of localX and non local is
        // TODO AGH figure
        edit.apply(rotation) {
            when(type) {
                RotationEdit.Type.YAW -> it.yaw = (it.yaw + degrees) % 360F
                RotationEdit.Type.PITCH -> it.pitch = (it.pitch + degrees) % 360F
                RotationEdit.Type.ROLL -> it.roll = (it.roll + degrees) % 360F
            }
        }
        edit(edit)
        player.sendActionBar(Component.text("Rotation = [yaw = ${rotation.yaw}, pitch = ${rotation.pitch}, roll = ${rotation.roll}]]").color(NamedTextColor.YELLOW))
    }

    private fun edit(edit: Edit<*>) {
        editHistory.addLast(edit)
        vehicle.update()
    }


    fun undo() {
        if(editHistory.isEmpty()) {
            return
        }
        val edit = editHistory.removeLast()
        edit.undo()
        vehicle.update()
        player.sendActionBar(Component.text().content("Undo Successful").color(NamedTextColor.GREEN))
    }
}

class VectorEdit : Edit<Vector3f>() {
    override fun save(obj: Vector3f): Vector3f {
        return obj.copy()
    }

    override fun revert(current: Vector3f, previousState: Vector3f) {
        current.set(previousState)
    }

    enum class Type {
        X, Y, Z
    }
}

class RotationEdit : Edit<ModelTransformation>() {
    override fun save(obj: ModelTransformation): ModelTransformation {
        return obj.copy()
    }

    override fun revert(current: ModelTransformation, previousState: ModelTransformation) {
        current.roll = previousState.roll
        current.pitch = previousState.pitch
        current.yaw = previousState.yaw
    }

    enum class Type {
        YAW, PITCH, ROLL
    }
}

abstract class Edit<T> {
    private var obj: T? = null
    private var save : T? = null
    fun apply(obj: T, action: (T) -> Unit) {
        this.obj = obj
        save = save(obj)
        action.invoke(obj)
    }

    abstract fun save(obj: T) : T

    fun undo() {
        obj?.let {
            save?.let { prev ->
                revert(it, prev)
                obj = null
                save = null
            }
        }
    }

    abstract fun revert(current: T, previousState: T)
}