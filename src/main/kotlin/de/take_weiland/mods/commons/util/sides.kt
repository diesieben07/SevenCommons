package de.take_weiland.mods.commons.util

import de.take_weiland.mods.commons.util.SCMethodHandles.getListeners
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */

val World.side inline get() = if (isRemote) Side.CLIENT else Side.SERVER
val World.isServer inline get() = !isRemote
val World.isClient inline get() = isRemote

val TileEntity.side inline get() = world.side
val TileEntity.isServer inline get() = world.isServer
val TileEntity.isClient inline get() = world.isClient

val Entity.side inline get() = world.side
val Entity.isServer inline get() = world.isServer
val Entity.isClient inline get() = world.isClient

val Container.side get(): Side = if (isClient) Side.CLIENT else Side.SERVER
val Container.isServer get() = getListeners(this).any { it is EntityPlayerMP }
val Container.isClient inline get() = !isServer