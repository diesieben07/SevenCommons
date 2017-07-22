package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.readBlockPos
import de.take_weiland.mods.commons.net.simple.AnySendable
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.net.simple.sendToTracking
import de.take_weiland.mods.commons.net.writeBlockPos
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * @author diesieben07
 */
interface SyncedContainerType<T : Any, DATA> {

    fun getServerWorld(obj: T): World

    fun sendPacket(obj: T, packet: AnySendable)

    fun <P> createChangedProperty(obj: T, property: SyncedProperty<P>, payload: P): ChangedProperty<P> = TODO()

    fun serialize(obj: T): DATA

    fun deserializeUntyped(player: EntityPlayer, data: DATA): T?

    fun serialize(buf: ByteBuf, obj: T)

    fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): T?

    companion object {

//        val REGISTRY = RegistryBuilder<SyncedContainerType<*, *>>()
//                .disableOverrides()
//                .disableSaving()
//                .setDefaultKey()

    }

}

fun <T : Any> findContainerType(value: T): SyncedContainerType<T, *> {
    @Suppress("UNCHECKED_CAST")
    return when (value) {
        is TileEntity -> TileEntitySyncedType
        is Entity -> EntitySyncedType
        else -> throw IllegalArgumentException("Unknown container $value for synced property.")
    } as SyncedContainerType<T, *>
}

inline fun <T : Any, DATA, reified R : T> SyncedContainerType<T, DATA>.deserialize(player: EntityPlayer, data: DATA): R? {
    return deserializeUntyped(player, data) as? R
}

inline fun <T : Any, DATA, reified R : T> SyncedContainerType<T, DATA>.deserialize(player: EntityPlayer, buf: ByteBuf): R? {
    return deserializeUntyped(player, buf) as? R
}

inline fun <T : Any> SimplePacket.sendTo(obj: T, containerType: SyncedContainerType<T, *>) {
    containerType.sendPacket(obj, this)
}

object TileEntitySyncedType : SyncedContainerType<TileEntity, BlockPos> {

    override fun getServerWorld(obj: TileEntity): World = obj.world

    override fun sendPacket(obj: TileEntity, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun <P> createChangedProperty(obj: TileEntity, property: SyncedProperty<P>, payload: P): ChangedProperty<P> {
        return TileEntityChangedProperty(property, obj.pos, payload)
    }

    override fun serialize(obj: TileEntity): BlockPos {
        return obj.pos
    }

    override fun deserializeUntyped(player: EntityPlayer, data: BlockPos): TileEntity? {
        return player.world.getTileEntity(data)
    }

    override fun serialize(buf: ByteBuf, obj: TileEntity) {
        buf.writeBlockPos(obj.pos)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): TileEntity? {
        return player.world.getTileEntity(buf.readBlockPos())
    }

}

internal class TileEntityChangedProperty<PAYLOAD>(property: SyncedProperty<PAYLOAD>, val pos: BlockPos, override val payload: PAYLOAD) : ChangedProperty<PAYLOAD>(property) {

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeBlockPos(pos)
    }

}


object EntitySyncedType : SyncedContainerType<Entity, Int> {

    override fun getServerWorld(obj: Entity): World = obj.world

    override fun sendPacket(obj: Entity, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun serialize(obj: Entity): Int {
        return obj.entityId
    }

    override fun deserializeUntyped(player: EntityPlayer, data: Int): Entity? {
        return player.world.getEntityByID(data)
    }

    override fun serialize(buf: ByteBuf, obj: Entity) {
        buf.writeInt(obj.entityId)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): Entity? {
        return player.world.getEntityByID(buf.readInt())
    }
}

object ContainerSyncedType : SyncedContainerType<Container, Byte> {

    override fun getServerWorld(obj: Container): World {
//        obj.listeners.fastForEach {
//            if (it is EntityPlayerMP) return it.world
//        }
        throw IllegalStateException("Container is not attached to server-side player.")
    }

    override fun sendPacket(obj: Container, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun serialize(obj: Container): Byte {
        return obj.windowId.toByte()
    }

    override fun deserializeUntyped(player: EntityPlayer, data: Byte): Container? {
        return player.openContainer.takeIf { it.windowId.toByte() == data }
    }

    override fun serialize(buf: ByteBuf, obj: Container) {
        buf.writeByte(obj.windowId)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): Container? {
        val id = buf.readByte()
        return player.openContainer.takeIf { it.windowId.toByte() == id }
    }
}

