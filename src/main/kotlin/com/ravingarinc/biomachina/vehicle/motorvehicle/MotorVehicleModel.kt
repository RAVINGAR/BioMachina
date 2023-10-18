package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.ravingarinc.biomachina.animation.*
import com.ravingarinc.biomachina.data.CollisionBox
import com.ravingarinc.biomachina.model.*
import com.ravingarinc.biomachina.vehicle.Part.Type
import com.ravingarinc.biomachina.vehicle.VehicleType
import org.bukkit.entity.Boat
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.min
class MotorVehicleModel(type: VehicleType) : VehicleModel(type) {
    val interaction = RootModel(SpawnableModel(Interaction::class.java, this) {
        it.interactionHeight = 2F
        it.interactionWidth = 2F
        it.isResponsive = true
    })

    private val innerChassis = object : CollidableModel, RootModel(ItemDisplayModel(type.chassis.model, interaction)) {
        init {
            (this.root as ItemDisplayModel).enableCollision(type.chassis.collision)
        }
        override val collisionBox: CollisionBox
            get() = (this.root as ItemDisplayModel).collisionBox
        override val isCollisionEnabled: Boolean
            get() = (this.root as ItemDisplayModel).isCollisionEnabled
        override val origin: Vector3f get() = (this.root as ItemDisplayModel).origin
        override val leftRotation: Quaternionf get() = (this.root as ItemDisplayModel).leftRotation
        override val rightRotation: Quaternionf get() =(this.root as ItemDisplayModel).rightRotation
        override val scale: Vector3f get() = (this.root as ItemDisplayModel).scale
        override val rotatingOrigin: Vector3f
            get() = (this.root as ItemDisplayModel).rotatingOrigin
        override var inverted: Boolean
            get() = (this.root as ItemDisplayModel).inverted
            set(value) {
                (this.root as ItemDisplayModel).inverted = value
            }

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

        override fun apply(yaw: Float, pitch: Float, roll: Float) {
            consumeEach {
                if(it is VectorModel) {
                    it.apply(yaw, pitch, roll)
                }
            }
        }

        override fun applyRelativeRotations() {
            consumeEach {
                if(it is VectorModel) {
                    it.applyRelativeRotations()
                }
            }
        }

        override fun applyAbsoluteRotations() {
            consumeEach {
                if(it is VectorModel) {
                    it.applyAbsoluteRotations()
                }
            }
        }
    }

    override val chassis: CollidableModel
        get() = innerChassis

    val frontWheelSet = ContainerModel()
    val rearWheelSet = ContainerModel()

    /**
     * If this model has front and rear wheels. False if no wheels or there are either 0 front or 0 rear.
     */
    val hasAllWheels: Boolean

    private val passengerSeats = ContainerModel()
    init {
        // always add from top to bottom
        //rotations may be fixed if we mount the chassis on another entity?
        add(interaction)

        interaction.add(innerChassis)

        innerChassis.add(frontWheelSet)
        innerChassis.add(rearWheelSet)
        innerChassis.add(passengerSeats)

        type.parts(Type.FRONT_WHEEL).forEach {
            val wheel = ItemDisplayModel(it.model, innerChassis)
            wheel.enableCollision(it.collision)
            frontWheelSet.add(wheel)
        }
        type.parts(Type.REAR_WHEEL).forEach {
            val wheel = ItemDisplayModel(it.model, innerChassis)
            wheel.enableCollision(it.collision)
            rearWheelSet.add(wheel)
        }

        hasAllWheels = (frontWheelSet.size > 0 && rearWheelSet.size > 0)

        //todo this
        for(i in 0 until type.passengerSeats) {
            passengerSeats.add(SpawnableModel(Boat::class.java, innerChassis) {
                it.setGravity(false)
                it.isInvulnerable = true
                it.isSilent = true
            })
        }
    }



    override fun apply(yaw: Float, pitch: Float, roll: Float) {
        this.chassis.apply(yaw, pitch, roll)
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
        controller.sendPackets(buildList {
            (innerChassis.root as ItemDisplayModel).let {
                it.applyRelativeRotations()
                this.add(it.getAnimationPacket(controller))
            }
            frontWheelSet.forEach { model ->
                (model as ItemDisplayModel).let {
                    it.applyRelativeRotations()
                    this.add(it.getAnimationPacket(controller))
                }
            }
            rearWheelSet.forEach { model ->
                (model as ItemDisplayModel).let {
                    it.applyRelativeRotations()
                    this.add(it.getAnimationPacket(controller))
                }
            }
        })
    }

    object Animations {
        fun MotorVehicleModel.buildIdleRotationAnimation(yaw: AtomicReference<Float>, pitch: AtomicReference<Float>, roll: AtomicReference<Float>) : Animation<MotorVehicleModel> {
            return object : PersistentAnimation<MotorVehicleModel>("idle_rot") {
                private val maxSteeringAngle = 0.610865F
                private var lastYaw: Float = yaw.get()
                private var lastPitch: Float = pitch.get()
                private var lastRoll: Float = roll.get()
                private var lastSteeringAngle: Float = 0F

                override fun tick(controller: AnimationController<MotorVehicleModel>) {

                    // todo use a look up table!
                    // https://www.analyzemath.com/trigonometry/trig_1.gif

                    val newYaw = yaw.get()
                    val newPitch = pitch.get()
                    val newRoll = roll.get()
                    val yawDiff = calculateRotationDifference(newYaw, lastYaw)
                    val pitchDiff = calculateRotationDifference(newPitch, lastPitch)
                    val rollDiff = calculateRotationDifference(newRoll, lastRoll)

                    if(yawDiff == 0F && pitchDiff == 0F && rollDiff == 0F) return

                    (innerChassis.root as VectorModel).let {
                        it.rotateYaw(yawDiff)
                        it.rotatePitch(pitchDiff)
                        it.rotateRoll(rollDiff)
                        it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.y, it.origin.z, newYaw, newPitch, newRoll))
                    }

                    val steeringAngle = calculateSteeringAngle(yawDiff)
                    val steeringDiff = yawDiff + steeringAngle - lastSteeringAngle
                    frontWheelSet.forEach { model ->
                        (model as VectorModel).let {
                            it.rotateYaw(steeringDiff)
                            it.rotatePitch(pitchDiff)
                            it.rotateRoll(rollDiff)
                            it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.y, it.origin.z, newYaw, newPitch, newRoll))
                        }
                    }

                    lastSteeringAngle = steeringAngle

                    rearWheelSet.forEach { model ->
                        (model as VectorModel).let {
                            it.rotateYaw(yawDiff)
                            it.rotatePitch(pitchDiff)
                            it.rotateRoll(rollDiff)
                            it.rotatingOrigin.set(calculateRotationOffset(it.origin.x, it.origin.y, it.origin.z, newYaw, newPitch, newRoll))
                        }
                    }
                    lastYaw = newYaw
                    lastPitch = newPitch
                    lastRoll = newRoll
                }

                /**
                 * Returns a rotational difference, or if difference is too small then 0 is returned.
                 */
                fun calculateRotationDifference(newValue: Float, lastValue: Float) : Float {
                    var diff = newValue - lastValue
                    if(diff == 0F) return 0F
                    val abs = abs(diff)
                    if(abs < 0.005F) return 0F
                    if(abs > Math.PI) {
                        if(diff < 0F) {
                            diff += AnimationUtilities.FULL_ROTATION_RADS
                        } else {
                            diff -= AnimationUtilities.FULL_ROTATION_RADS
                        }
                    }
                    return diff
                }


                fun calculateSteeringAngle(difference: Float): Float {
                    if(difference == 0F) return 0F
                    val v = maxSteeringAngle * (min(0.15708F, abs(difference)) / 0.15708F)
                    return if(difference < 0F) v * -1 else v
                }
            }
        }

        fun MotorVehicleModel.buildWheelRotationAnimation(speed: AtomicReference<Float>) : Animation<MotorVehicleModel> {
            return object : PersistentIntervalAnimation<MotorVehicleModel>("wheel_rot", 5) {
                private val wheelCircumference: Float
                init {
                    (frontWheelSet.first() as? CollidableModel).let {
                        wheelCircumference = if(it == null) {
                            2F * Math.PI.toFloat() * 0.25F
                        } else {
                            Math.PI.toFloat() * (it.collisionBox.height)
                        }
                    }
                }
                private val wheelRotations : Float = Math.PI.toFloat() / 5F
                private val factor = 1000F / 60F / 60F / 20F * 5F

                override fun tick(controller: AnimationController<MotorVehicleModel>) {
                    val speedInKmH = speed.get()
                    if(speedInKmH == 0F) return
                    val newSpeed = min(speedInKmH, 100F) * factor
                    val rotationRads : Float = (wheelRotations * newSpeed / wheelCircumference)
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