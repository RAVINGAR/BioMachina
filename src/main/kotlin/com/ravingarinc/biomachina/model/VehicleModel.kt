package com.ravingarinc.biomachina.model

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.api.I.log
import com.ravingarinc.biomachina.animation.Animation
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.PersistentAnimation
import com.ravingarinc.biomachina.data.VehicleType
import com.ravingarinc.biomachina.model.api.*
import org.bukkit.Location
import org.bukkit.entity.*
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level

// todo mayb egive each model an ID so we can track parent hierachy
class VehicleModel(private val type: VehicleType) : RootModel(SpawnableModel(Boat::class.java, null) {
    it.setGravity(false)
    it.isInvulnerable = true
    it.isSilent = true
}, null) {

    private val chassis = object : VectorModel, RootModel(DisplayModel(type.chassis), this) {
        override val origin: Vector3f
            get() = (this.root as DisplayModel).origin
        override val leftRotation: Quaternionf
            get() = (this.root as DisplayModel).leftRotation
        override val rightRotation: Quaternionf
            get() = (this.root as DisplayModel).rightRotation
        override val scale: Vector3f
            get() = (this.root as DisplayModel).scale
    }

    val interaction = SpawnableModel(Interaction::class.java, chassis) {
        it.interactionHeight = 2F
        it.interactionWidth = 2F
        it.isResponsive = true
    }

    private val frontWheelSet = ContainerModel(chassis)
    private val rearWheelSet = ContainerModel(chassis)
    private val passengerSeats = ContainerModel(chassis)
    init {
        // always add from top to bottom
        add(chassis)

        chassis.add(frontWheelSet)
        chassis.add(rearWheelSet)
        chassis.add(passengerSeats)
        chassis.add(interaction)

        type.frontWheels.forEach {
            frontWheelSet.add(DisplayModel(it, chassis))
        }
        type.rearWheels.forEach {
            rearWheelSet.add(DisplayModel(it, chassis))
        }

        for(i in 0 until type.passengerSeats) {
            passengerSeats.add(SpawnableModel(Boat::class.java, passengerSeats) {
                it.setGravity(false)
                it.isInvulnerable = true
                it.isSilent = true
            })
        }
    }

    fun mountDriver(player: Player) : Boolean {
        if(root.getPassengerAmount() < 2) {
            root.entity?.let {
                it.addPassenger(player)
                return true
            }
        }
        return false
    }

    fun mountPassenger(player: Player) : Boolean {
        var mounted = false
        passengerSeats.forEach { seat ->
            if(seat is EntityModel && seat.getPassengerAmount() == 0) {
                seat.entity?.let {
                    it.addPassenger(player)
                    mounted = true
                    return@forEach
                }
            }
        }
        return mounted
    }

    object Animations {
        fun VehicleModel.buildIdleRotationAnimation(yaw: AtomicReference<Float>) : Animation<VehicleModel> {
            return object : PersistentAnimation<VehicleModel>("idle_rot", 1) {
                override fun tick(controller: AnimationController<VehicleModel>) {
                    val model = this@buildIdleRotationAnimation
                    val yawBytes = (yaw.get() * 256.0F / 360.0F).toInt().toByte()
                    val pitchBytes = (0).toByte()

                    controller.sendPacket(buildList {
                        this.add(controller.createPacket(PacketType.Play.Server.ENTITY_LOOK) {
                            it.integers.write(0, model.getEntityId())
                            it.bytes
                                .write(0, yawBytes)
                                .write(1, pitchBytes)
                        })
                        model.frontWheelSet.forEach { wheel ->
                            (wheel as EntityModel).let { entityWheel ->
                                this.add(controller.createPacket(PacketType.Play.Server.ENTITY_LOOK) {
                                    it.integers.write(0, entityWheel.getEntityId())
                                    it.bytes
                                        .write(0, yawBytes)
                                        .write(1, pitchBytes)
                                })
                            }
                        }
                        model.rearWheelSet.forEach { wheel ->
                            (wheel as EntityModel).let { entityWheel ->
                                this.add(controller.createPacket(PacketType.Play.Server.ENTITY_LOOK) {
                                    it.integers.write(0, entityWheel.getEntityId())
                                    it.bytes
                                        .write(0, yawBytes)
                                        .write(1, pitchBytes)
                                })
                            }
                        }
                    })
                }
            }
        }
    }
}