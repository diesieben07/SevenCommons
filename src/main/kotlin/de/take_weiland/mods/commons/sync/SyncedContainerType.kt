package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.internal.SevenCommons
import de.take_weiland.mods.commons.util.fastForEach
import de.take_weiland.mods.commons.util.listeners
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

}

object TileEntitySyncedType : SyncedContainerType<TileEntity> {

    override fun getWorld(obj: TileEntity): World = obj.world
}

object EntitySyncedType : SyncedContainerType<Entity> {

    override fun getWorld(obj: Entity): World = obj.world

}

object ContainerSyncedType : SyncedContainerType<Container> {

    override fun getWorld(obj: Container): World {
        obj.listeners.fastForEach {
            if (it is EntityPlayerMP) return it.world
        }
        return SevenCommons.proxy.clientWorld
    }

}