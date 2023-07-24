package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.AnimationHandler
import com.ravingarinc.biomachina.animation.AnimationUtilities
import com.ravingarinc.biomachina.api.round
import com.ravingarinc.biomachina.api.toRadians
import com.ravingarinc.biomachina.model.CollidableModel
import com.ravingarinc.biomachina.vehicle.Vehicle
import com.ravingarinc.biomachina.vehicle.VehicleManager
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicleModel.Animations.buildIdleRotationAnimation
import com.ravingarinc.biomachina.vehicle.motorvehicle.MotorVehicleModel.Animations.buildWheelRotationAnimation
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
import kotlin.math.abs
import kotlin.math.atan

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

    override var isMountable: Boolean = true

    override val yaw = AtomicReference(0F)
    override val pitch = AtomicReference(0F)
    override val roll = AtomicReference(0F)
    override val speed = AtomicReference(0F)

    val terrainHeight: Float = 1.0F // Can only travel one block heights

    private var innerHasGravity: Boolean = false
    var hasGravity: Boolean
        get() = innerHasGravity
        set(value) {
            if(value) {
                if(!innerHasGravity) {
                    innerHasGravity = true
                    model.entity?.setGravity(true)
                }
            } else {
                if(innerHasGravity) {
                    innerHasGravity = false
                    model.entity?.setGravity(false)
                }
            }
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
        model.apply(yaw.get().toRadians() * -1F)
    }

    override fun buildAnimationController(handler: AnimationHandler): AnimationController<*> {
        val controller = AnimationController(handler, model)
        with(model) {
            controller.animate(this.buildIdleRotationAnimation(yaw, pitch, roll))
            controller.animate(this.buildWheelRotationAnimation(speed))
            // todo more animations!
        }

        return controller
    }

    override fun start(player: Player) {

    }

    override fun stop(player: Player) {

    }

    /**
     * Executed as sync every 5 ticks
     */
    override fun tick() {
        model.entity!!.let {
            val location = it.location
            val vector = location.toVector()


            tickSpeed(vector, it)
            tickHeight(location, it)

            // todo Fuel Consumption

            lastLocation = vector
        }
    }

    private fun tickSpeed(location: Vector, entity: Entity) {
        speed.set(String.format("%.2f", lastLocation.distanceSquared(location)).toFloat())
    }

    private fun tickHeight(location: Location, entity: Entity) {
        val speed = speed.get()
        if(speed == 0F) {
            entity.velocity = entity.velocity.setY(0)
            return
        }
        if(model.hasAllWheels) {
            tickHeightGround(speed, location, entity)
        } else {
            tickHeightHover(speed, location, entity)
        }
    }

    private fun tickHeightGround(speed: Float, location: Location, entity: Entity) {
        val yaw = yaw.get()
        val pitch = pitch.get()
        val roll = roll.get()

        var frontDiff = 0F
        var frontVector: Vector? = null
        for(wheel in model.frontWheelSet) {
            frontVector = (wheel as CollidableModel).collisionBox.getCentreBottom(yaw, pitch, roll)
            val diff = rayTraceHeight(location, frontVector)
            if(abs(diff) > abs(frontDiff)) {
                frontDiff = diff
            }
        }
        var rearDiff = 0F
        var rearVector: Vector? = null
        for(wheel in model.rearWheelSet) {
            rearVector = (wheel as CollidableModel).collisionBox.getCentreBottom(yaw, pitch, roll)
            val diff = rayTraceHeight(location, rearVector)
            if(abs(diff) > abs(rearDiff)) {
                rearDiff = diff
            }
        }
        if(frontDiff == 0F && rearDiff == 0F) {
            warn("1. Debug -> Cruising!")
            val velocity = entity.velocity
            if(velocity.y != 0.0) {
                if(pitch != 0F) this.pitch.set(0F)
                if(roll != 0F) this.roll.set(0F)
                entity.velocity = velocity.setY(0.0)
            }
            return
        }

        if(frontDiff == rearDiff) {
            if(pitch != 0F) this.pitch.set(0F)
            if(roll != 0F) this.roll.set(0F)
        } else {
            if(frontVector == null || rearVector == null) return
            val xDiff = (frontVector.x - rearVector.x).toFloat()
            val zDiff = (frontVector.z - rearVector.z).toFloat()
            val horizontalDistance = AnimationUtilities.quickSqrt((xDiff * xDiff) + (zDiff * zDiff))
            // The angle we are calculating is from the rear to the front. AKA The right angle is at the base of the front
            // wheel. As such the hypotenuse in this case is the unknown, as we know only the opposite and adjacent.
            // Opposite side (vertical), is the yDiff, whilst horizontal distance is the adjacent
            val newPitch = atan(abs(rearDiff - frontDiff) / horizontalDistance) // Determine pitch
            if(newPitch != pitch) this.pitch.set(newPitch)

            // Todo if you really want roll calculations? So its like climbing terrain properly lol
        }
        val averageDiff = (frontDiff + rearDiff) / 2F
        if(averageDiff == 0F) {
            // Cruising
            warn("1. Debug -> Cruising - b!")
            val velocity = entity.velocity
            if(velocity.y != 0.0) entity.velocity = velocity.setY(0F)
        } else if(averageDiff > 0F) {
            warn("1. Debug -> Moving Up")
            if(frontDiff > terrainHeight || rearDiff > terrainHeight) {
                warn("  2. Debug -> Next Block is too high!")
                stop(location, entity)
            } else {
                warn("  2. Debug -> Scaling terrain!")
                entity.velocity = entity.velocity.setY(averageDiff / 5.0)
            }
        } else {
            warn("1. Debug -> Moving Down")
            entity.velocity = entity.velocity.setY(averageDiff / 5.0)
        }
    }

    private fun tickHeightHover(speed: Float, location: Location, entity: Entity) {

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
        }
    }

    private fun rayTraceHeight(location: Location, modelLocation: Vector) : Float {
        val bottom = location.clone().add(modelLocation)
        val result =
            world.rayTraceBlocks(bottom, Vector(0, -1, 0), (terrainHeight + type.height).toDouble(), FluidCollisionMode.NEVER, true)
                ?: return -2F
        world.spawnParticle(Particle.VILLAGER_HAPPY, bottom, 5) // <-- Todo remove this
        return (result.hitPosition.y - location.y + type.height).toFloat().round(1)
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
            yaw.set(it.location.yaw)
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