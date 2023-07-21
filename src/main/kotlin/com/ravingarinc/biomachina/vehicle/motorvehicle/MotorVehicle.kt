package com.ravingarinc.biomachina.vehicle.motorvehicle

import com.ravingarinc.api.module.warn
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.AnimationHandler
import com.ravingarinc.biomachina.api.toRadians
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

    private var lastY: Double = 324.0

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

    override val world: World
        get() = model.entity?.world ?: throw IllegalStateException("Attempted to get world for vehicle that wasn't spawned yet!")

    override fun create(location: Location) {
        lastLocation = location.toVector()
        lastY = location.y
        model.create(location.x, location.y, location.z, location.world!!)
    }
    override fun destroy() {
        engineRunning.setRelease(false)
        model.destroy()
    }

    override fun apply() {
        model.apply(yaw.get().toRadians() * -1F)
    }

    override fun buildAnimationController(handler: AnimationHandler): AnimationController<*> {
        val controller = AnimationController(handler, model)
        with(model) {
            controller.animate(this.buildIdleRotationAnimation(yaw))
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
            tickHeight(location, vector, it)

            // todo Fuel Consumption

            lastLocation = vector
        }
    }

    private fun tickSpeed(location: Vector, entity: Entity) {
        speed.set(String.format("%.2f", lastLocation.distanceSquared(location)).toFloat())
    }

    private fun tickHeight(location: Location, vector: Vector, entity: Entity) {
        val world = entity.world
        // TODO Wait, should we handle this completely differently. Basically we rayTrace a line through the world
        // There should be multiple ray traces,
        /**
         *
         * Vehicle is moving across
         * We should check height after we've moved up or down
         *                  |
         *       <-->  |    |
         *
         */
        /*
        Case for Moving Forward
        - Raytrace Forward

        Case for Moving Backwards

        Case for Turning Right

        Case for Turning Left

        All of the
         */

        /*
        Height

        Ray Trace at the nose
         */

        val speed = speed.get()
        if(speed == 0F) {
            entity.velocity = entity.velocity.setY(0)
            return
        }
        val yaw = yaw.get()
        val pitch = pitch.get()
        val collision = model.collisionBox
        val front = collision.getRelativeFront(yaw, pitch, 0F) // Roll only applicable for flying vehicles

        // Todo ray trace for each front wheels if moving forward
        val position = location.clone().add(front)

        world.spawnParticle(Particle.VILLAGER_HAPPY, position, 5) // Todo remove this <--
        val min = collision.min
        val max = collision.max
        world.spawnParticle(Particle.VILLAGER_ANGRY, location.clone().add(min), 5) // Todo remove this <--
        world.spawnParticle(Particle.VILLAGER_ANGRY, location.clone().add(max), 5) // Todo remove this <--
        val result = world.rayTraceBlocks(position, Vector(0, -1, 0), collision.height + (terrainHeight * 2), FluidCollisionMode.NEVER, true)
        val velocity = entity.velocity
        if(result == null) {
            warn("1. Debug -> No block found!")
            // No block found! Then FALL!
            entity.velocity = velocity.setY(-0.4)
            lastY = location.y
        } else {
            val hit = result.hitPosition.y
            val yDiff = hit - lastY
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
            lastY = hit
            //None of this will work most likely lol
        }
        // Ray trace draws from (basically where the wheels touch the ground)

        /*
        val upperBlock = world.getBlockAt(position.blockX, position.blockY + terrainHeight.toInt() + 1, position.blockZ)
        if(!upperBlock.type.isAir && !upperBlock.isPassable) {
            // If not air and not passable then we should break here
            // The vehicle should crash() basically
            crash()
            return
        }
        val midBlock = world.getBlockAt(position.blockX, position.blockY + terrainHeight.toInt(), position.blockZ)
        if(!midBlock.type.isAir && !upperBlock.isPassable) {

        }

        for(i in (-terrainHeight.toInt())..terrainHeight.toInt()) {
            val block = world.getBlockAt(lastLocation.blockX, lastLocation.blockY - i, lastLocation.blockZ)
            if(block.type.isAir || block.isPassable) continue

        }*/
        // If we exit this for loop by passing the chassis height it means that the vehicle is falling
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
        hasGravity = true
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