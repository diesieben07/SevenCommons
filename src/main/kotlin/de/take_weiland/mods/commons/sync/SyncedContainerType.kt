package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.net.readBlockPos
import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.simple.sendToTracking
import de.take_weiland.mods.commons.net.writeBlockPos
import de.take_weiland.mods.commons.util.clientPlayer
import de.take_weiland.mods.commons.util.clientThread
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.ConcurrentHashMap

/**
 * @author diesieben07
 */
abstract class SyncedContainerType<T : Any>(baseType: Class<T>) : PacketChannel {

    init {
        @Suppress("LeakingThis")
        val previousValue = containerTypeMap.putIfAbsent(baseType, this)
        require(previousValue == null) {
            "SyncedContainerType for base type ${baseType.name} already registered."
        }
    }

    abstract fun getWorld(obj: T): World

    abstract fun createChangedPropertyList(obj: T): ChangedPropertyList<T>

    protected abstract fun readContainerData(buf: ByteBuf, player: EntityPlayer): T?

    final override val autoRelease: Boolean
        get() = false

    final override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
        clientThread {
            try {
                val container = readContainerData(buf, clientPlayer)
                if (container != null) {
                    val size = buf.readVarInt()
                    val accessor = PropertyAccessorCache.get(container.javaClass)

                    var i = 0
                    while (i < size) {
                        val propertyId = buf.readVarInt()

                        val property = PropertyAccessorHelpers.accessProperty(accessor, container, propertyId)
                        property?.receivePayload(buf)

                        i += 2
                    }
                }
            } finally {
                buf.release()
            }
        }
    }

}

internal fun <T : Any> findContainerType(value: T): SyncedContainerType<T> {
    @Suppress("UNCHECKED_CAST")
    return ContainerTypeCache[value.javaClass] as SyncedContainerType<T>? ?: throw IllegalArgumentException("Unknown container $value for synced property.")
}

internal val containerTypeMap = ConcurrentHashMap<Class<*>, SyncedContainerType<*>>()

internal object ContainerTypeCache : ClassValue<SyncedContainerType<*>?>() {

    override fun computeValue(type: Class<*>): SyncedContainerType<*>? {
        var currentType = type
        while (currentType != Any::class.java) {
            val containerType = containerTypeMap[currentType]
            if (containerType != null) return containerType
            currentType = currentType.superclass
        }
        return null
    }

}

object TileEntitySyncedType : SyncedContainerType<TileEntity>(TileEntity::class.java) {

    override val channel: String
        get() = "sevencommons_syncte"

    override fun getWorld(obj: TileEntity): World = obj.world

    override fun createChangedPropertyList(obj: TileEntity): TileEntityChangedPropertyList {
        return TileEntityChangedPropertyList(obj.pos)
    }

    override fun readContainerData(buf: ByteBuf, player: EntityPlayer): TileEntity? {
        return player.world.getTileEntity(buf.readBlockPos())
    }

}

class TileEntityChangedPropertyList(internal val pos: BlockPos) : ChangedPropertyList<TileEntity>() {

    override val containerType: SyncedContainerType<TileEntity>
        get() = TileEntitySyncedType

    override fun send(obj: TileEntity) {
        sendToTracking(obj)
    }

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun getContainer(player: EntityPlayer): TileEntity? {
        return player.world.getTileEntity(pos)
    }

}


object EntitySyncedType : SyncedContainerType<Entity>(Entity::class.java) {

    override val channel: String
        get() = "sevencommons:syncent"

    override fun createChangedPropertyList(obj: Entity): ChangedPropertyList<Entity> {
        return EntityChangedPropertyList(obj.entityId)
    }

    override fun getWorld(obj: Entity): World = obj.world

    override fun readContainerData(buf: ByteBuf, player: EntityPlayer): Entity? {
        return player.world.getEntityByID(buf.readInt())
    }
}

internal class EntityChangedPropertyList(val id: Int) : ChangedPropertyList<Entity>() {

    override val containerType: SyncedContainerType<Entity>
        get() = EntitySyncedType

    override fun send(obj: Entity) {
        sendToTracking(obj)
    }

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeInt(id)
    }

    override fun getContainer(player: EntityPlayer): Entity? {
        return player.world.getEntityByID(id)
    }

}

object ContainerSyncedType : SyncedContainerType<Container>(Container::class.java) {

    override val channel: String
        get() = "sevencommons:syncct"

    override fun createChangedPropertyList(obj: Container): ChangedPropertyList<Container> {
        return ContainerChangedPropertyList(obj.windowId)
    }

    override fun getWorld(obj: Container): World {
        for (listener in obj.listeners) {
            if (listener is EntityPlayerMP) return listener.world
        }
        return clientPlayer.world
    }

    override fun readContainerData(buf: ByteBuf, player: EntityPlayer): Container? {
        return player.openContainer.takeIf { it.windowId == buf.readByte().toInt() }
    }
}

internal class ContainerChangedPropertyList(val windowId: Int) : ChangedPropertyList<Container>() {

    override val containerType: SyncedContainerType<Container>
        get() = ContainerSyncedType

    override fun send(obj: Container) {
        sendToTracking(obj)
    }

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeByte(windowId)
    }

    override fun getContainer(player: EntityPlayer): Container? {
        return player.openContainer.takeIf { it.windowId == windowId }
    }

}

