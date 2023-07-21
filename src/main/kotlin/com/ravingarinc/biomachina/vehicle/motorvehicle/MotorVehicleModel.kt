package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.ravingarinc.biomachina.animation.*
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.model.*
import com.ravingarinc.biomachina.vehicle.Part.Type
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Boat
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.min

// Todo change this abstract class as well
class MotorVehicleModel(type: VehicleType) : VehicleModel(type) {
    val interaction = RootModel(SpawnableModel(Interaction::class.java, this) {
        it.interactionHeight = 2F
        it.interactionWidth = 2F
        it.isResponsive = true
    })

    // Delegated chassis object.
    private val chassis = object : VectorModel, RootModel(ItemDisplayModel(type.chassis.model, interaction)) {
        val item : ItemStack get() = (this.root as ItemDisplayModel).item
        override val origin: Vector3f get() = (this.root as ItemDisplayModel).origin
        override val leftRotation: Quaternionf get() = (this.root as ItemDisplayModel).leftRotation
        override val rightRotation: Quaternionf get() =(this.root as ItemDisplayModel).rightRotation
        override val scale: Vector3f get() = (this.root as ItemDisplayModel).scale
        override var inverted: Boolean
            get() = (this.root as ItemDisplayModel).inverted
            set(value) {
                (this.root as ItemDisplayModel).inverted = value
            }

        override val rotatingOrigin: Vector3f
            get() = (this.root as ItemDisplayModel).rotatingOrigin

        override var absYaw: Float
            get() = (this.root as ItemDisplayModel).absYaw
            set(value) {
                (this.root as ItemDisplayModel).absYaw = value
            }
        override var absPitch: Float
            get() = (this.root as ItemDisplayModel).absPitch
            set(value) {
                (this.root as ItemDisplayModel).absPitch = value
            }
        override var absRoll: Float
            get() = (this.root as ItemDisplayModel).absRoll
            set(value) {
                (this.root as ItemDisplayModel).absRoll = value
            }

        override var relYaw: Float
            get() = (this.root as ItemDisplayModel).relYaw
            set(value) {
                (this.root as ItemDisplayModel).relYaw = value
            }
        override var relPitch: Float
            get() = (this.root as ItemDisplayModel).relPitch
            set(value) {
                (this.root as ItemDisplayModel).relPitch = value
            }
        override var relRoll: Float
            get() = (this.root as ItemDisplayModel).relRoll
            set(value) {
                (this.root as ItemDisplayModel).relRoll = value
            }

        override fun apply(yaw: Float) {
            (this.root as ItemDisplayModel).apply(yaw)
        }

        override fun update() {
            (this.root as ItemDisplayModel).update()
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
            frontWheelSet.add(ItemDisplayModel(it.model, chassis))
        }
        type.parts(Type.REAR_WHEEL).forEach {
            rearWheelSet.add(ItemDisplayModel(it.model, chassis))
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



    override fun apply(yaw: Float) {
        this.chassis.forEach {
            if(it is VectorModel) {
                it.apply(yaw)
            }
        }
        super.apply(yaw)
    }

    @Deprecated("Not needed")
    fun getExemptIds() : Set<Int> {
        return buildSet {
            this.add(chassis.getEntityId())
            frontWheelSet.forEach { this.add((it as EntityModel).getEntityId())}
            rearWheelSet.forEach { this.add((it as EntityModel).getEntityId())}
        }
    }

    override fun mountDriver(player: Player) : Boolean {
        if(root.getPassengerAmount() < 2) {
            root.entity?.let {
                it.addPassenger(player)
                return true
            }
        }
        return false
    }

    override fun mountPassenger(player: Player) : Boolean {
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

    override fun update(controller: AnimationController<*>) {
        controller.sendPacket(buildList {
            (chassis.root as ItemDisplayModel).let {
                it.update()
                this.add(it.getAnimationPacket(controller))
            }
            frontWheelSet.forEach { model ->
                (model as ItemDisplayModel).let {
                    it.update()
                    this.add(it.getAnimationPacket(controller))
                }
            }
            rearWheelSet.forEach { model ->
                (model as ItemDisplayModel).let {
                    it.update()
                    this.add(it.getAnimationPacket(controller))
                }
            }
        }.toTypedArray())
    }

    object Animations {
        fun MotorVehicleModel.buildIdleRotationAnimation(yaw: AtomicReference<Float>) : Animation<MotorVehicleModel> {
            return object : PersistentAnimation<MotorVehicleModel>("idle_rot") {
                private val maxSteeringAngle = 0.610865F
                private var lastYaw: Float = yaw.get()
                private var lastSteeringAngle: Float = 0F

                override fun tick(controller: AnimationController<MotorVehicleModel>) {

                    // todo
                    // use a look up table!
                    // https://www.analyzemath.com/trigonometry/trig_1.gif

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
                    //val newYaw = (((yaw.get() - 360F) % 360) * -1).toRadians() // To convert to 360* conversion!
                    val newYaw = yaw.get().toRadians() * -1F
                    //val newYaw = asin(sin(yaw.get().toRadians()))
                    if(lastYaw == newYaw) return
                    var diff = newYaw - lastYaw

                    val abs = abs(diff)
                    if(abs < 0.005F) return
                    if(abs > Math.PI) {
                        if(diff < 0F) {
                            diff += AnimationUtilities.FULL_ROTATION
                        } else {
                            diff -= AnimationUtilities.FULL_ROTATION
                        }
                    }
                    // todo OKAY now that we are using radians, for some reason its bugging out a little
                    // todo fix the diff being -350
                    // this occurs because the previous yaw might be 355 whilst the new one is 5

                    val steeringAngle = calculateSteeringAngle(diff)
                    val steeringDiff = diff + steeringAngle - lastSteeringAngle
                    frontWheelSet.forEach { model ->
                        (model as VectorModel).let {
                            it.rotateYaw(steeringDiff)
                            it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.z, newYaw)) // need add relative
                        }
                    }
                    lastSteeringAngle = steeringAngle

                    //I.log(Level.WARNING, "Yaw is $newYaw, diff = $diff,with angle of $steeringAngle")

                    (chassis.root as VectorModel).let {
                        it.rotateYaw(diff)
                        it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.z, newYaw)) // need add relative
                    }

                    rearWheelSet.forEach { model ->
                        (model as VectorModel).let {
                            it.rotateYaw(diff)
                            it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.z, newYaw)) // need add relative
                        }
                    }
                    lastYaw = newYaw
                }


                fun calculateSteeringAngle(difference: Float): Float {
                    val v = maxSteeringAngle * (min(0.15708F, abs(difference)) / 0.15708F)
                    return if(difference < 0F) v * -1 else v
                }
            }
        }

        fun MotorVehicleModel.buildWheelRotationAnimation(speed: AtomicReference<Float>) : Animation<MotorVehicleModel> {
            return object : PersistentIntervalAnimation<MotorVehicleModel>("wheel_rot", 5) {
                private val wheelCircumference: Float
                init {
                    (frontWheelSet.first() as? VectorModel).let {
                        wheelCircumference = if(it == null) {
                            2F * Math.PI.toFloat() * 0.25F
                        } else {
                            2F * Math.PI.toFloat() * (0.25F * it.scale.x)
                        }
                    }
                }
                private val wheelRotations : Float = Math.PI.toFloat() / 2F
                override fun tick(controller: AnimationController<MotorVehicleModel>) {
                    val speedSquared = speed.get()
                    if(speedSquared == 0F) return
                    val currentSpeed = AnimationUtilities.quickSqrt(speedSquared) / 5F
                    //if(currentSpeed == 0F) return
                    val rotationRads : Float = wheelRotations * currentSpeed / wheelCircumference

                    frontWheelSet.forEach { model ->
                        (model as VectorModel).rotatePitch(rotationRads)
                    }
                    rearWheelSet.forEach { model ->
                        (model as VectorModel).rotatePitch(rotationRads)
                    }
                }

            }
        }
    }
}