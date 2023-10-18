package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.google.common.util.concurrent.AtomicDouble
import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.AnimationUtilities
import com.ravingarinc.biomachina.api.AtomicInput
import com.ravingarinc.biomachina.api.round
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.model.CollidableModel
import com.ravingarinc.biomachina.model.ContainerModel
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicleModel.Animations.buildIdleRotationAnimation
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicleModel.Animations.buildWheelRotationAnimation
import com.ravingarinc.biomachina.vehicle.stat.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.*
import kotlin.system.measureTimeMillis

/**
 * Vehicle represents a Model linked to a driveable entity.
 * In main case it is a horse.
 * What we can do as well is have a custom gui!, since we give leather horse armour and make a GUI
 * based on the colour sort of thing!
 * As the player rotates with the horse, the vehicle will not rotate instantly.
 *
 * A vehicle does have whats considered an RPM, basically it takes a bit to get to full RPM before the max
 * speed of the horse is unlocked,
 * any let of "throttle" aka forward causes this number to drop and then the speed washes off as well
 *
 * Need to consider motion in N, E, S, W
 * If the player is pressing just W -> then the cars wheels is straight and it's accelerating
 * if the player is pressing W and either A or D then the cars wheels is turning slightly in that direction
 * If the player is pressing JUST A or D, then wheel is tracking straight but vehicle is moving sideways
 * The issue is the with horses, it doesnt really handle like a car
 * The alternative is BOATS!
 * Maybe it would be worth having blocks placed underneath a boat
 *
 * Constructor must be called as sync!
 */
class MotorVehicle(override val uuid: UUID, override val type: MotorVehicleType) : Vehicle {
    private val model = MotorVehicleModel(type)
    private val engineRunning = AtomicBoolean(false)
    private var driver: Player? = null
    private val passengers: MutableSet<LivingEntity> = HashSet()

    override val statHolder: StatHolder = StatHolder()

    override var isMountable: Boolean = true

    override val input: AtomicInput = AtomicInput()

    /**
     * The actual speed squared, using the difference in location as a measure
     */
    val actualSpeedSquared: AtomicDouble = AtomicDouble(0.0)

    /**
     * Atomic yaw in radians
     */
    override val yaw = AtomicReference(0F)

    /**
     * Atomic pitch in radians
     */
    override val pitch = AtomicReference(0F)

    /**
     * Atomic roll in radians
     */
    override val roll = AtomicReference(0F)

    /**
     * Speed in kilometres per hour
     */
    override val speed = AtomicReference(0F)

    private val velocity = Vector()

    val topSpeed: Float get() = statHolder.getStat(Speed).value()
    val terrainHeight: Float get() = statHolder.getStat(TerrainHeight).value()

    val acceleration: Float get() = statHolder.getStat(Acceleration).value()

    val brakingPower: Float get() = statHolder.getStat(BrakingPower).value()

    override val animationController: AnimationController<*> by lazy {
        val controller = AnimationController(model)
        with(model) {
            controller.animate(this.buildIdleRotationAnimation(yaw, pitch, roll))
            controller.animate(this.buildWheelRotationAnimation(speed))
            // todo more animations!
        }
        return@lazy controller
    }

    companion object {
        private val minimalRange = (-0.05F..0.05F)
        private val traversalFactor = 5F
        private val speedConverterFactor = 1000F / 60F / 60F / 20F
        private val swayBarDiff = 0.5F
    }

    override val isDestroyed: Boolean
        get() = model.entity == null

    private lateinit var lastLocation: Vector

    override val entityId: Int
        get() = model.entity?.entityId ?: -1

    override val world: World by lazy {
        model.entity?.world ?: throw IllegalStateException("Attempted to get world for vehicle that wasn't spawned yet!")
    }

    override fun create(location: Location) {
        lastLocation = location.toVector()
        model.create(location.x, location.y + type.height, location.z, location.world!!)
    }
    override fun destroy() {
        engineRunning.setRelease(false)
        model.destroy()
    }

    override fun location(): Location {
        return Location(world, lastLocation.x, lastLocation.y, lastLocation.z)
    }

    override fun apply() {
        model.apply(yaw.get(), pitch.get(), roll.get())
    }

    override fun start(player: Player) {

    }

    override fun stop(player: Player) {

    }

    /**
     * Executed as sync every 1 tick
     */
    override fun tick() {
        val time = measureTimeMillis {
            model.entity!!.let {
                val motion = input.motion()
                // Positive is forward, negative is backwards.
                val forwardMotion = motion.z.toFloat()
                val location = it.location


                tickSpeed(forwardMotion)

                // Todo tickCollision()
                tickHeight(location, it)
                tickVelocity()

                // Todo tickFuel()

                val vector = location.toVector()
                actualSpeedSquared.set(lastLocation.distanceSquared(vector))
                lastLocation = vector
                it.velocity = velocity
            }
        }
        if(time > 1) {
            warn("Vehicle Tick took $time ms!")
        }
    }

    override fun tickUI() {
        driver?.let {
            val builder = Component.text()
            builder.append(Component.text("< Speed - ").color(NamedTextColor.YELLOW))
            val speed = speed.get().toInt()
            val size = speed.toString().length
            for(i in size until 3) {
                builder.append(Component.text(" "))
            }
            builder.append(Component.text("$speed km/h").color(NamedTextColor.GOLD))
            builder.append(Component.text(" >").color(NamedTextColor.GRAY))
            it.sendActionBar(builder)
        }
    }

    private fun tickVelocity() {
        val speed = this.speed.get()
        if(speed == 0F) {
            velocity.x = 0.0
            velocity.z = 0.0
            return
        }
        // Remember speed is always in kilometres an hour so we must calculate here.
        val metersPerTick = speed * speedConverterFactor
        val yaw = yaw.get() * -1F
        velocity.setX(-sin(yaw) * metersPerTick)
        velocity.setZ(cos(yaw) * metersPerTick)
    }

    private fun tickSpeed(forwardMotion: Float) {
        val lastSpeed = speed.get()
        if(forwardMotion == 0F) {
            // If 0, we should slow down at a static rate
            if(lastSpeed == 0F) return
            speed.set(max(0F, lastSpeed - 1F))
            return
        } else if(forwardMotion > 0F) {
            val top = topSpeed
            if(lastSpeed < top) speed.set(min(lastSpeed + (acceleration / 20F * forwardMotion), topSpeed))
        } else {
            speed.set(max(lastSpeed + (brakingPower / 20F * forwardMotion), -10F))
        }// TODO Fix me there is wheel sync shift phase somewhere. Something must have changed...
    }

    private fun tickHeight(location: Location, entity: Entity) {
        // Todo add a suitable check here to not run the below if the vehicle is stationary
        if(speed.get() == 0F && velocity.isZero && input.hasNoInput) return
        if(model.hasAllWheels) {
            tickHeightGround(location, entity)
        } else {
            tickHeightHover(location, entity)
        }
    }

    private fun tickHeightGround(location: Location, entity: Entity) {
        val yaw = yaw.get()
        val pitch = pitch.get()
        val roll = roll.get()

        val chassisHeight = model.chassis.collisionBox.height
        val frontInfo = performTraces(model.frontWheelSet, location, chassisHeight, yaw, pitch, roll)
        val rearInfo = performTraces(model.rearWheelSet, location, chassisHeight, yaw, pitch, roll)

        if((minimalRange.contains(frontInfo.height) && minimalRange.contains(rearInfo.height))
            || (frontInfo.height == 0F && rearInfo.height == 0F)
            || frontInfo.zeroTraces + rearInfo.zeroTraces >= (frontInfo.totalTraces + rearInfo.totalTraces) * 0.75) {
            //warn("1. Debug -> Cruising!")
            if(velocity.y != 0.0) {
                if(pitch != 0F) this.pitch.set(0F)
                if(roll != 0F) this.roll.set(0F)
                velocity.setY(0.0)
            }
            return
        }

        if(frontInfo.height == rearInfo.height) {
            if(pitch != 0F) this.pitch.set(0F)
            if(roll != 0F) this.roll.set(0F)
        } else {
            val xDiff = (frontInfo.vector.x - rearInfo.vector.x).toFloat()
            val zDiff = (frontInfo.vector.z - rearInfo.vector.z).toFloat()
            val horizontalDistance = AnimationUtilities.quickSqrt((xDiff * xDiff) + (zDiff * zDiff))
            // The angle we are calculating is from the rear to the front. AKA The right angle is at the base of the front
            // wheel. As such the hypotenuse in this case is the unknown, as we know only the opposite and adjacent.
            // Opposite side (vertical), is the yDiff, whilst horizontal distance is the adjacent
            val factor = if(frontInfo.height < 0F) 2F else -2F
            val absDiff = abs(rearInfo.height - frontInfo.height)
            val newPitch = atan(absDiff / horizontalDistance) / factor // Determine pitch
            if(newPitch != pitch) this.pitch.set(newPitch)

            /* Todo Implement Roll Calculations Properly, the below bugs out
            // Since, we set above that the y of the vector is actually the difference in each case
            if(oldFrontVector == null || oldFrontVector.y == frontVector.y) {
                if(roll != 0F) this.roll.set(0F)
            } else {
                val xRollDiff = (frontVector.x - oldFrontVector.x).toFloat()
                val zRollDiff = (frontVector.z - oldFrontVector.z).toFloat()
                val rollDist = AnimationUtilities.quickSqrt((xRollDiff * xRollDiff) + (zRollDiff * zRollDiff))
                val newRoll = atan(absDiff / rollDist) / factor
                if(newRoll != roll) this.roll.set(newRoll)
            }
            */
        }
        val fHeight = frontInfo.height
        val rHeight = rearInfo.height
        val averageDiff = (((fHeight + rHeight) / 2F) / traversalFactor)
        //warn("Front Diff = $fHeight | Rear Diff = $rHeight")

        if(fHeight == rHeight) {
            // If these things are the same then we can sorta assume like either floating or in the ground
            if(fHeight > 0F) {
                //warn("1. Debug -> Moving Up")
                velocity.setY(abs(averageDiff))
            } else {
                //warn("1. Debug -> Moving Down")
                velocity.setY(abs(averageDiff) * -1F)
            }
        } else if(fHeight > rHeight) {
            // If greater than, means that moving up
            //warn("1. Debug -> Moving Up")
            if(frontInfo.height > terrainHeight) {
                //warn("  2. Debug -> Next Block is too high!")
                stop(location, entity) // This doesn't actually stop the boat!
            } else {
                //warn("  2. Debug -> Scaling terrain!")
                velocity.setY(abs(averageDiff))
            }
        } else {
            //warn("1. Debug -> Moving Down")
            velocity.setY(abs(averageDiff) * -1F)
        }
    }

    private fun performTraces(container: ContainerModel, location: Location, chassisHeight: Float, yaw: Float, pitch: Float, roll: Float) : TraceInfo {
        var zeroTraces = 0
        var highestValue = 0F
        var highestVector = Vector()
        for(model in container) {
            val vector = (model as CollidableModel).collisionBox.getCentreBottom(yaw, pitch, roll)
            val diff = rayTraceHeight(location, vector, chassisHeight)
            if(diff == 0F) zeroTraces++
            if(abs(diff) > abs(highestValue)) {
                highestValue = diff
                highestVector = vector
            }
        }
        return TraceInfo(highestValue, highestVector, zeroTraces, container.size)
    }

    private fun tickHeightHover(location: Location, entity: Entity) {
        TODO("Update me!")
        /*
        val yaw = yaw.get()
        val pitch = pitch.get()
        val roll = roll.get()
        val collision = model.chassis.collisionBox
        val front = collision.getRelativeFront(yaw, pitch, roll)

        // Todo ray trace for each front wheels if moving forward
        val position = location.clone().add(front)

        val result = world.rayTraceBlocks(position, Vector(0, -1, 0), (collision.height + terrainHeight + type.height).toDouble(), FluidCollisionMode.NEVER, true)
        val velocity = entity.velocity
        if(result == null) {
            warn("1. Debug -> No block found!")
            // No block found! Then FALL!
            entity.velocity = velocity.setY(-0.4)
        } else {
            val yDiff = (result.hitPosition.y - location.y + type.height).round(1)
            // if yDiff is positive, vehicle is moving up
            if (yDiff == 0.0) {
                // Not moving vertically
                warn("1. Debug -> Cruising!")
                if(velocity.y != 0.0) entity.velocity = velocity.setY(0)
            } else if (yDiff > 0) {
                // Greater than 0 -> Moving up
                warn("1. Debug -> Moving up")
                if(yDiff > terrainHeight) {
                    warn("  2. Debug -> Next Block is too high!")
                    // If its greater than terrainheight, then vehicle should like crash lol
                    stop(location, entity)
                } else {
                    warn("  2. Debug -> Scaling terrain!")
                    // Vehicle scales terrain but slows down a bit
                    entity.velocity = velocity.setY(yDiff / 5.0)
                }
            } else {
                // Less than 0 -> Moving down
                warn("1. Debug -> Moving down")
                entity.velocity = velocity.setY(yDiff / 5.0) // yDiff doesn't need to be negative since it already is
            }
        }*/
    }

    private fun rayTraceHeight(location: Location, modelLocation: Vector, chassisHeight: Float) : Float {
        val bottom = location.clone().add(modelLocation)
        //world.spawnParticle(Particle.VILLAGER_HAPPY, bottom, 5) // <-- Todo remove this
        val bottomY = bottom.y
        bottom.y = bottomY + chassisHeight + terrainHeight
        //world.spawnParticle(Particle.VILLAGER_HAPPY, bottom, 5) // <-- Todo remove this
        val result =
            world.rayTraceBlocks(bottom, Vector(0, -1, 0), (terrainHeight * 2F + type.height + chassisHeight).toDouble(), FluidCollisionMode.NEVER, true)
                ?: return -0.4F * traversalFactor
        return (result.hitPosition.y - bottomY + type.height).toFloat().round(1)
    }

    /**
     * Stop this vehicle abruptly
     */
    fun stop(location: Location, entity: Entity) {
        entity.velocity = Vector()
    }

    fun crash(location: Location, entity: Entity) {
        // The name of this method is misleading (should be stop)
        // This should crash in different ways based on the speed of the vehicle.
        entity.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.8F)
        entity.world.spawnParticle(Particle.EXPLOSION_HUGE, location,  1)
        // handle enabling gravity maybe?
    }

    /**
     * Executed as sync every vehicle move event
     */
    override fun sync() {
        model.entity!!.let {
            yaw.set(it.location.yaw.toRadians() * -1F)
        }
    }

    override fun isRunning() : Boolean {
        return engineRunning.acquire;
    }

    override fun interact(manager: VehicleManager, player: Player, hand: EquipmentSlot) {
        if(player == driver) {
            if(engineRunning.acquire) {
                engineRunning.setRelease(false)
                // play stop sound
                player.sendMessage("${ChatColor.GRAY}You stopped the engine!")
            } else {
                engineRunning.setRelease(true)
                player.sendMessage("${ChatColor.GRAY}You started the engine!")
                model.entity!!.let {
                    it.setGravity(false)
                }
            }
            // open gui or something (or rather make horn sound!)
        } else {
            if(driver == null || !passengers.contains(player)) {
                if(mount(player)) {
                    manager.registerMount(player, this)
                }
            }
        }
    }

    override fun boundingBox() : Interaction? {
        return model.interaction.entity as? Interaction
    }

    /**
     * Returns true if player successfully mounted
     */
    override fun mount(player: Player) : Boolean {
        if(!isMountable) return false

        if(driver == null && model.mountDriver(player)) {
            driver = player
            return true
        }
        if(passengers.size < type.passengerSeats) {
            if(model.mountPassenger(player)) {
                passengers.add(player)
                return true
            }
        } else {
            player.sendMessage("${ChatColor.GRAY}That vehicle is full!")
        }
        return false
    }

    /**
     * Dismounts a player without ejecting them
     */
    override fun dismount(player: Player) : Boolean {
        // handle passenger leaving todo calculate damage when jumping out of a moving car!
        if(player == driver) {
            driver = null
            return true
        } else if(passengers.remove(player)) {
            return true
        }
        return false
    }

    override fun forceDismount(player: Player): Boolean {
        if(player == driver) {
            player.eject()
            driver = null
            return true
        } else if(passengers.remove(player)) {
            player.eject()
            return true
        }
        return false
    }

    override fun dismountAll() {
        driver?.eject()
        driver = null
        passengers.forEach {
            it.eject()
        }
        passengers.clear()
    }
}

data class TraceInfo(val height: Float, val vector: Vector, val zeroTraces: Int, val totalTraces: Int)