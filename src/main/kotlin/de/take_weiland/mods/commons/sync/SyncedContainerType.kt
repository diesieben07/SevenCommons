package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.internal.SevenCommons
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.util.fastForEach
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * @author diesieben07
 */
interface SyncedContainerType<in T> {

    fun getWorld(obj: T): World

    fun sendPacket(obj: T, packet: SimplePacket)

}

inline fun <T> SimplePacket.sendTo(obj: T, containerType: SyncedContainerType<T>) {
    containerType.sendPacket(obj, this)
}

object TileEntitySyncedType : SyncedContainerType<TileEntity> {

    override fun getWorld(obj: TileEntity): World = obj.world

    override fun sendPacket(obj: TileEntity, packet: SimplePacket) {
//        packet.sendToTracking(obj)
    }
}

object EntitySyncedType : SyncedContainerType<Entity> {

    override fun getWorld(obj: Entity): World = obj.world

    override fun sendPacket(obj: Entity, packet: SimplePacket) {
//        packet.sendToTracking(obj)
    }
}

object ContainerSyncedType : SyncedContainerType<Container> {

    override fun getWorld(obj: Container): World {
        obj.listeners.fastForEach {
            if (it is EntityPlayerMP) return it.world
        }
        return SevenCommons.proxy.clientWorld
    }

    override fun sendPacket(obj: Container, packet: SimplePacket) {
//        packet.sendToTracking(obj)
    }
}