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
interface SyncedContainerType<T : Any> {

    fun getServerWorld(obj: T): World

    fun sendPacket(obj: T, packet: AnySendable)

    fun createChangedPropertyList(obj: T): ChangedPropertyList<T>

    fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): T?

}

fun <T : Any> findContainerType(value: T): SyncedContainerType<T> {
    @Suppress("UNCHECKED_CAST")
    return when (value) {
        is TileEntity -> TileEntitySyncedType
        is Entity -> EntitySyncedType
        else -> throw IllegalArgumentException("Unknown container $value for synced property.")
    } as SyncedContainerType<T>
}

inline fun <T : Any> SimplePacket.sendTo(obj: T, containerType: SyncedContainerType<T>) {
    containerType.sendPacket(obj, this)
}

object TileEntitySyncedType : SyncedContainerType<TileEntity> {

    override fun getServerWorld(obj: TileEntity): World = obj.world

    override fun sendPacket(obj: TileEntity, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun createChangedPropertyList(obj: TileEntity): ChangedPropertyList<TileEntity> {
        return TileEntityChangedPropertyList(obj.pos)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): TileEntity? {
        return player.world.getTileEntity(buf.readBlockPos())
    }

}

internal class TileEntityChangedPropertyList(val pos: BlockPos) : ChangedPropertyList<TileEntity>() {

    override val containerType: SyncedContainerType<TileEntity>
        get() = TileEntitySyncedType

    override val channel: String
        get() = "sevencommons_syncte"

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun getContainer(player: EntityPlayer): TileEntity? {
        return player.world.getTileEntity(pos)
    }

}


object EntitySyncedType : SyncedContainerType<Entity> {

    override fun createChangedPropertyList(obj: Entity): ChangedPropertyList<Entity> {
        return EntityChangedPropertyList(obj.entityId)
    }

    override fun getServerWorld(obj: Entity): World = obj.world

    override fun sendPacket(obj: Entity, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): Entity? {
        return player.world.getEntityByID(buf.readInt())
    }
}

internal class EntityChangedPropertyList(val id: Int) : ChangedPropertyList<Entity>() {

    override val containerType: SyncedContainerType<Entity>
        get() = EntitySyncedType

    override val channel: String
        get() = "sevencommons:syncent"

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeInt(id)
    }

    override fun getContainer(player: EntityPlayer): Entity? {
        return player.world.getEntityByID(id)
    }

}

object ContainerSyncedType : SyncedContainerType<Container> {

    override fun createChangedPropertyList(obj: Container): ChangedPropertyList<Container> {
        return ContainerChangedPropertyList(obj.windowId)
    }

    override fun getServerWorld(obj: Container): World {
        throw IllegalStateException("Container is not attached to server-side player.")
    }

    override fun sendPacket(obj: Container, packet: AnySendable) {
        packet.sendToTracking(obj)
    }

    override fun deserializeUntyped(player: EntityPlayer, buf: ByteBuf): Container? {
        val id = buf.readByte()
        return player.openContainer.takeIf { it.windowId.toByte() == id }
    }
}

internal class ContainerChangedPropertyList(val windowId: Int) : ChangedPropertyList<Container>() {

    override val containerType: SyncedContainerType<Container>
        get() = ContainerSyncedType

    override val channel: String
        get() = "sevencommons:syncct"

    override fun writeContainerData(buf: ByteBuf) {
        buf.writeByte(windowId)
    }

    override fun getContainer(player: EntityPlayer): Container? {
        return player.openContainer.takeIf { it.windowId == windowId }
    }

}

