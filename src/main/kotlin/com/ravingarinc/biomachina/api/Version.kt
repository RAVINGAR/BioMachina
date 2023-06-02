package com.ravingarinc.biomachina.api

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.ravingarinc.api.I
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*
import java.util.logging.Level


/**
 * Versions representing usages of different protocols
 * See https://wiki.vg/Protocol_version_numbers
 */
sealed class Version(
    val major: Int,
    val minor: Int,
    val patch: IntRange,
    val protocol: Int,
    val packFormat: Int,
    val names: Array<String>
) {
    /**
     * Protocol Articles
     *
     * https://wiki.vg/index.php?title=Entity_metadata&oldid=18191
     */
    sealed class V1_19_4(major : Int = 1, minor : Int = 19, patch : IntRange = 4..4, protocol: Int = 762, packFormat : Int = 13, names: Array<String> = arrayOf("1.19.4")) :
        Version(major, minor, patch, protocol, packFormat, names) {
        override val indexedEntities: Map<EntityType, Int> = buildMap {
            this[EntityType.ALLAY] = 0
            this[EntityType.AREA_EFFECT_CLOUD] = 1
            this[EntityType.ARMOR_STAND] = 2
            this[EntityType.ARROW] = 3
            this[EntityType.AXOLOTL] = 4
            this[EntityType.BAT] = 5
            this[EntityType.BEE] = 6
            this[EntityType.BLAZE] = 7
            this[EntityType.BLOCK_DISPLAY] = 8
            this[EntityType.BOAT] = 9
            //this[EntityType.CAMEL] = 10
            this[EntityType.CAT] = 11
            this[EntityType.CAVE_SPIDER] = 12
            this[EntityType.CHEST_BOAT] = 13
            this[EntityType.MINECART_CHEST] = 14
            this[EntityType.CHICKEN] = 15
            this[EntityType.COD] = 16
            this[EntityType.MINECART_COMMAND] = 17
            this[EntityType.COW] = 18
            this[EntityType.CREEPER] = 19
            this[EntityType.DOLPHIN] = 20
            this[EntityType.DONKEY] = 21
            this[EntityType.DRAGON_FIREBALL] = 22
            this[EntityType.DROWNED] = 23
            this[EntityType.EGG] = 24
            this[EntityType.ELDER_GUARDIAN] = 25
            this[EntityType.ENDER_CRYSTAL] = 26
            this[EntityType.ENDER_DRAGON] = 27
            this[EntityType.ENDER_PEARL] = 28
            this[EntityType.ENDERMAN] = 29
            this[EntityType.ENDERMITE] = 30
            this[EntityType.EVOKER] = 31
            this[EntityType.EVOKER_FANGS] = 32
            this[EntityType.THROWN_EXP_BOTTLE] = 33
            this[EntityType.EXPERIENCE_ORB] = 34
            this[EntityType.ENDER_SIGNAL] = 35
            this[EntityType.FALLING_BLOCK] = 36
            this[EntityType.FIREWORK] = 37
            this[EntityType.FOX] = 38
            this[EntityType.FROG] = 39
            this[EntityType.MINECART_FURNACE] = 40
            this[EntityType.GHAST] = 41
            this[EntityType.GIANT] = 42
            this[EntityType.GLOW_ITEM_FRAME] = 43
            this[EntityType.GLOW_SQUID] = 44
            this[EntityType.GOAT] = 45
            this[EntityType.GUARDIAN] = 46
            this[EntityType.HOGLIN] = 47
            this[EntityType.MINECART_HOPPER] = 48
            this[EntityType.HORSE] = 49
            this[EntityType.HUSK] = 50
            this[EntityType.ILLUSIONER] = 51
            this[EntityType.INTERACTION] = 52
            this[EntityType.IRON_GOLEM] = 53
            this[EntityType.DROPPED_ITEM] = 54
            this[EntityType.ITEM_DISPLAY] = 55
            this[EntityType.ITEM_FRAME] = 56
            this[EntityType.FIREBALL] = 57
            this[EntityType.LEASH_HITCH] = 58
            this[EntityType.LIGHTNING] = 59
            this[EntityType.LLAMA] = 60
            this[EntityType.LLAMA_SPIT] = 61
            this[EntityType.MAGMA_CUBE] = 62
            this[EntityType.MARKER] = 63
            this[EntityType.MINECART] = 64
            this[EntityType.MUSHROOM_COW] = 65
            this[EntityType.MULE] = 66
            this[EntityType.OCELOT] = 67
            this[EntityType.PAINTING] = 68
            this[EntityType.PANDA] = 69
            this[EntityType.PARROT] = 70
            this[EntityType.PHANTOM] = 71
            this[EntityType.PIG] = 72
            this[EntityType.PIGLIN] = 73
            this[EntityType.PIGLIN_BRUTE] = 74
            this[EntityType.PILLAGER] = 75
            this[EntityType.POLAR_BEAR] = 76
            this[EntityType.SPLASH_POTION] = 77
            this[EntityType.PUFFERFISH] = 78
            this[EntityType.RABBIT] = 79
            this[EntityType.RAVAGER] = 80
            this[EntityType.SALMON] = 81
            this[EntityType.SHEEP] = 82
            this[EntityType.SHULKER] = 83
            this[EntityType.SHULKER_BULLET] = 84
            this[EntityType.SILVERFISH] = 85
            this[EntityType.SKELETON] = 86
            this[EntityType.SKELETON_HORSE] = 87
            this[EntityType.SLIME] = 88
            this[EntityType.SMALL_FIREBALL] = 89
            //this[EntityType.SNIFFER] = 90
            this[EntityType.SNOWMAN] = 91
            this[EntityType.SNOWBALL] = 92
            this[EntityType.MINECART_MOB_SPAWNER] = 93
            this[EntityType.SPECTRAL_ARROW] = 94
            this[EntityType.SPIDER] = 95
            this[EntityType.SQUID] = 96
            this[EntityType.STRAY] = 97
            this[EntityType.STRIDER] = 98
            this[EntityType.TADPOLE] = 99
            this[EntityType.TEXT_DISPLAY] = 100
            this[EntityType.PRIMED_TNT] = 101
            this[EntityType.MINECART_TNT] = 102
            this[EntityType.TRADER_LLAMA] = 103
            this[EntityType.TRIDENT] = 104
            this[EntityType.TROPICAL_FISH] = 105
            this[EntityType.TURTLE] = 106
            this[EntityType.VEX] = 107
            this[EntityType.VILLAGER] = 108
            this[EntityType.VINDICATOR] = 109
            this[EntityType.WANDERING_TRADER] = 110
            this[EntityType.WARDEN] = 111
            this[EntityType.WITCH] = 112
            this[EntityType.WITHER] = 113
            this[EntityType.WITHER_SKELETON] = 114
            this[EntityType.WITHER_SKULL] = 115
            this[EntityType.WOLF] = 116
            this[EntityType.ZOGLIN] = 117
            this[EntityType.ZOMBIE] = 118
            this[EntityType.ZOMBIE_HORSE] = 119
            this[EntityType.ZOMBIE_VILLAGER] = 120
            this[EntityType.ZOMBIFIED_PIGLIN] = 121
            this[EntityType.PLAYER] = 122
            this[EntityType.FISHING_HOOK] = 123
        }
        companion object : V1_19_4()
    }

    protected abstract val indexedEntities: Map<EntityType, Int>

    fun getEntityTypeId(type: EntityType) : Int {
        return indexedEntities[type]
            ?: throw IllegalStateException("Cannot get entity id for entity type '${type.name}' as version ${getVersionName()} does not contain this entity!")
    }

    fun getVersionName() : String {
        return names[names.size - 1]
    }

    fun transformDisplayEntity(watcher: WrappedDataWatcher, delay: Int, duration: Int, translation: Vector3f, scale: Vector3f, rotLeft: Quaternionf, rotRight: Quaternionf, item: ItemStack) : PacketContainer {
        val packet = Version.protocol.createPacket(PacketType.Play.Server.ENTITY_METADATA, true)

        // see https://www.spigotmc.org/threads/unable-to-modify-entity-metadata-packet-using-protocollib-1-19-3.582442/#post-4517187

        packet.integers.write(0, watcher.entity.entityId)

        watcher.setObject(8, integerSerializer, delay)
        watcher.setObject(9, integerSerializer, duration)
        watcher.setObject(10, vectorSerializer, translation)
        watcher.setObject(11, vectorSerializer, scale)
        watcher.setObject(12, quaternionSerializer, rotLeft)
        watcher.setObject(13, quaternionSerializer, rotRight)

        // need to fill other defaults here!
        watcher.setObject(22, itemSerializer, item)
        watcher.setObject(23, byteSerializer, java.lang.Byte.valueOf("8"))

        // todo 1.19.3 and above things!
        val dataList : List<WrappedDataValue> = buildList {
            // todo convert this to just use it in the first place.
            watcher.watchableObjects.filterNotNull().forEach { entry ->
                val data : WrappedDataWatcher.WrappedDataWatcherObject = entry.watcherObject
                this.add(WrappedDataValue(data.index, data.serializer, entry.rawValue))
            }
        }
        packet.dataValueCollectionModifier.write(0, dataList)

        return packet
    }

    override fun equals(other: Any?): Boolean {
        if(other is Version) {
            return other.major == this.major && other.minor == this.minor && other.patch == this.patch && other.protocol == this.protocol && other.packFormat == this.packFormat
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(major, minor, patch, protocol, packFormat)
    }

    companion object {
        protected val protocol: ProtocolManager = ProtocolLibrary.getProtocolManager()
        protected val byteSerializer = WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)
        protected val integerSerializer = WrappedDataWatcher.Registry.get(java.lang.Integer::class.java)
        protected val floatSerializer = WrappedDataWatcher.Registry.get(java.lang.Float::class.java)
        protected val vectorSerializer = WrappedDataWatcher.Registry.get(Vector3f::class.java)
        protected val quaternionSerializer = WrappedDataWatcher.Registry.get(Quaternionf::class.java)
        protected val itemSerializer = WrappedDataWatcher.Registry.getItemStackSerializer(false)
    }
}

object Versions {
    val serverVersion: Version by lazy {
        val version = Bukkit.getServer().bukkitVersion // expecting Format of 1.18.2-R0.1-SNAPSHOT
        val parts = version.substring(0, version.indexOf('-')).split(".")

        val major = parts[0].toIntOrNull()
            ?: throw IllegalStateException("Could not parse version major from version $version!")
        val minor = parts[1].toIntOrNull()
            ?: throw IllegalStateException("Could not parse version minor from version $version!")
        val patch = (if (parts.size > 2) parts[2].toIntOrNull() else 0)
            ?: throw IllegalStateException("Could not parse version patch from version $version!")

        for (v in values) {
            if (v.major == major && v.minor == minor && v.patch.contains(patch)) {
                return@lazy v
            }
        }
        throw IllegalStateException("Could not get server version as this plugin does not support the version $version!")
    }

    val values: Array<Version> = arrayOf(Version.V1_19_4)

    private val protocolMap: Map<Int, Version> = buildMap {
        for (version in values) {
            this[version.protocol] = version
        }
    }

    fun getFromProtocol(protocol: Int): Version {
        return protocolMap[protocol] ?: throw IllegalArgumentException("Unsupported protocol version $protocol")
    }

    fun validateVersion() {
        I.log(Level.INFO, "Successfully loaded version handler for versions ${buildString {
            for(i in serverVersion.names.indices) {
                this.append(serverVersion.names[i])
                if(i + 1 < serverVersion.names.size) {
                    this.append(", ")
                }
            }
        }}.");
    }
}