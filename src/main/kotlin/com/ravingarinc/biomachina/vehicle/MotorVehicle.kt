package com.ravingarinc.biomachina.vehicle

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.ravingarinc.biomachina.animation.Animation
import com.ravingarinc.biomachina.animation.AnimationController
import com.ravingarinc.biomachina.animation.PersistentAnimation
import com.ravingarinc.biomachina.data.VehicleType
import com.ravingarinc.biomachina.model.VehicleModel
import com.ravingarinc.biomachina.model.VehicleModel.Animations.buildIdleRotationAnimation
import com.ravingarinc.biomachina.protocol.AnimationHandler
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Interaction
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.HashMap

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
 */
class MotorVehicle(override val uuid: UUID, val type: VehicleType,
                   creator: (AnimationHandler, MotorVehicle) -> AnimationController<*>
) : Vehicle {
    private val model = VehicleModel(type)
    private val engineRunning = AtomicBoolean(false)
    private var driver: Player? = null
    private val passengers: MutableSet<LivingEntity> = HashSet()

    private val currentYaw = AtomicReference(0F)

    private var lastSync: Long = 0L

    override val entityId: Int
        get() = model.entity?.entityId ?: -1

    override val world: World
        get() = model.entity?.world ?: throw IllegalStateException("Attempted to get world for vehicle that wasn't spawned yet!")

    override fun create(location: Location) {
        model.create(location.x, location.y, location.z, location.world!!)
    }
    override fun destroy() {
        engineRunning.setRelease(false)
        model.destroy()
    }

    override fun buildAnimationController(handler: AnimationHandler): AnimationController<*> {
        val controller = AnimationController(handler, model)
        with(model) {
            controller.animate(this.buildIdleRotationAnimation(currentYaw))
            // todo more animations!
        }

        return controller
    }

    override fun start(player: Player) {

    }

    override fun stop(player: Player) {

    }

    /**
     * Sync tick task
     */
    override fun tick() {

    }

    override fun sync() {
        val time = System.currentTimeMillis()
        if(time > lastSync + 50L) {
            lastSync = time
            currentYaw.set(model.entity!!.location.yaw)
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
        return model.interaction.castEntity
    }

    /**
     * Returns true if player successfully mounted
     */
    override fun mount(player: Player) : Boolean {
        if(driver == null) {
            if(model.mountDriver(player)) {
                driver = player
                return true
            }
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

    object Factory {
        // see https://blog.logrocket.com/understanding-kotlin-design-patterns/#factory-abstract-factory-provider-model-kotlin-method
        private val vehicleTypes: MutableMap<String, VehicleType> = HashMap()

        fun getType(typeName: String) : VehicleType? {
            return vehicleTypes[typeName.lowercase()];
        }
        fun clear() {
            vehicleTypes.clear()
        }

        fun hasType(typeName: String) : Boolean {
            return vehicleTypes.containsKey(typeName)
        }

        fun getTypes() : Set<String> {
            return vehicleTypes.keys
        }

        fun add(type: VehicleType) {
            vehicleTypes[type.identifier] = type
        }
    }
}