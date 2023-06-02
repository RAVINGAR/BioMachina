package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.biomachina.animation.Animation
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.PersistentAnimation
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.model.api.*
import com.ravingarinc.biomachina.vehicle.VehiclePart.Type
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Boat
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Todo change this abstract class as well
class MotorVehicleModel(private val type: VehicleType) : RootModel(SpawnableModel(Boat::class.java, null) {
    it.setGravity(false)
    it.isInvulnerable = true
    it.isSilent = true
}) {
    val interaction = RootModel(SpawnableModel(Interaction::class.java, this) {
        it.interactionHeight = 2F
        it.interactionWidth = 2F
        it.isResponsive = true
    })

    // Delegated chassis object.
    private val chassis = object : VectorModel, RootModel(DisplayModel(type.chassis().model, interaction)) {
        val item : ItemStack get() = (this.root as DisplayModel).item
        override val origin: Vector3f get() = (this.root as DisplayModel).origin
        override val leftRotation: Quaternionf get() = (this.root as DisplayModel).leftRotation
        override val rightRotation: Quaternionf get() =(this.root as DisplayModel).rightRotation
        override val scale: Vector3f get() = (this.root as DisplayModel).scale

        override val rotatingOrigin: Vector3f
            get() = (this.root as DisplayModel).rotatingOrigin

        override var absYaw: Float
            get() = (this.root as DisplayModel).absYaw
            set(value) {
                (this.root as DisplayModel).absYaw = value
            }
        override var absPitch: Float
            get() = (this.root as DisplayModel).absPitch
            set(value) {
                (this.root as DisplayModel).absPitch = value
            }
        override var absRoll: Float
            get() = (this.root as DisplayModel).absRoll
            set(value) {
                (this.root as DisplayModel).absRoll = value
            }

        override var relYaw: Float
            get() = (this.root as DisplayModel).relYaw
            set(value) {
                (this.root as DisplayModel).relYaw = value
            }
        override var relPitch: Float
            get() = (this.root as DisplayModel).relPitch
            set(value) {
                (this.root as DisplayModel).relPitch = value
            }
        override var relRoll: Float
            get() = (this.root as DisplayModel).relRoll
            set(value) {
                (this.root as DisplayModel).relRoll = value
            }

        override fun reapply() {
            (this.root as DisplayModel).reapply()
        }
    }

    private val frontWheelSet = ContainerModel()
    private val rearWheelSet = ContainerModel()
    private val passengerSeats = ContainerModel()
    init {
        // always add from top to bottom
        //rotations may be fixed if we mount the chassis on another entity?
        add(interaction)

        interaction.add(chassis)

        chassis.add(frontWheelSet)
        chassis.add(rearWheelSet)
        chassis.add(passengerSeats)


        type.parts(Type.FRONT_WHEEL).forEach {
            frontWheelSet.add(DisplayModel(it.model, chassis))
        }
        type.parts(Type.REAR_WHEEL).forEach {
            rearWheelSet.add(DisplayModel(it.model, chassis))
        }

        //todo this
        for(i in 0 until type.passengerSeats) {
            passengerSeats.add(SpawnableModel(Boat::class.java, chassis) {
                it.setGravity(false)
                it.isInvulnerable = true
                it.isSilent = true
            })
        }
    }

    fun reapply() {
        this.chassis.forEach {
            if(it is VectorModel) {
                it.reapply()
            }
        }
    }

    @Deprecated("Not needed")
    fun getExemptIds() : Set<Int> {
        return buildSet {
            this.add(chassis.getEntityId())
            frontWheelSet.forEach { this.add((it as EntityModel).getEntityId())}
            rearWheelSet.forEach { this.add((it as EntityModel).getEntityId())}
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

    fun getAllTransformationPackets(controller: AnimationController<MotorVehicleModel>) : Array<PacketContainer> {
        return buildList {
            (chassis.root as DisplayModel).let {
                this.add(it.getTransformationPacket(controller, duration = 1))
            }
            frontWheelSet.forEach { model ->
                (model as DisplayModel).let {
                    this.add(it.getTransformationPacket(controller, duration = 1))
                }
            }
            rearWheelSet.forEach { model ->
                (model as DisplayModel).let {
                    this.add(it.getTransformationPacket(controller, duration = 1))
                }
            }
        }.toTypedArray()
    }

    object Animations {
        fun MotorVehicleModel.buildIdleRotationAnimation(yaw: AtomicReference<Float>) : Animation<MotorVehicleModel> {
            return object : PersistentAnimation<MotorVehicleModel>("idle_rot") {
                private var lastYaw: Float = yaw.get()

                override fun tick(controller: AnimationController<MotorVehicleModel>) {


                    /*
                    Right Rotation rotating the X axis will always spin the wheel (as if its moving)
                    Regardless of the orientation of everything else
                     */

                    /**
                     * Need to consider of course. So if a boat's yaw is 0, then the entity models yaw is 180!
                     * But generally the rotational yaw is also 0
                     * Basically every tick we should apply a differential.
                     *
                     */
                    val newYaw = ((yaw.get() - 360F) % 360) * -1 // To convert to 360* conversion!
                    if(lastYaw == newYaw) return
                    val diff = newYaw - lastYaw
                    if(abs(diff) < 0.5F) return
                    lastYaw = newYaw
                    /**
                     * 0* is (1 * x, 1 * z)
                     * 90* is (1 * z, -1 * x)
                     * 180* is (-1 * x, -1 * z)
                     * 270* is (-1 * z, 1 * x)
                     */
                    chassis.forEach {
                        if(it is VectorModel) {
                            it.rotateYaw(diff)
                            it.rotatingOrigin.set(calculateTranslation(it.origin.x, it.origin.z, newYaw)) // need add relative
                        }
                    }
                }
                fun calculateTranslation(x: Float, z: Float, yaw: Float): Vector3f {
                    // Convert degrees to radians
                    val radians = yaw.toRadians()

                    // Calculate the sine and cosine of the angle
                    val sinY = sin(radians)
                    val cosY = cos(radians)

                    // Calculate the x and z coordinates based on the rotation
                    return Vector3f(
                        (cosY * x + sinY * z) - x,
                        0F,
                        (-sinY * x + cosY * z) - z)
                }
            }
        }
    }
}