package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeDirection;

import java.util.Collections;
import java.util.Set;

public final class Entities {

	private Entities() { }

	/**
	 * gets an NBTTagCompound for storing custom data for this entity<br>
	 * uses {@link Entity#getEntityData()} and {@link EntityPlayer#PERSISTED_NBT_TAG} for players to keep the data persistent across deaths
	 * @param entity
	 * @return
	 */
	public static NBTTagCompound getData(Entity entity) {
		if (entity instanceof EntityPlayer) {
			return NBT.getOrCreateCompound(entity.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG);
		} else {
			return entity.getEntityData();
		}
	}

	/**
	 * Utility method, equal to
	 * <code>NBT.getOrCreateCompound(Entities.getData(entity), subKey)</code>
	 * @param entity
	 * @param subKey
	 * @return
	 */
	public static NBTTagCompound getData(Entity entity, String subKey) {
		return NBT.getOrCreateCompound(getData(entity), subKey);
	}
	
	/**
	 * get all players tracking the given entity<br>
	 * If you want to send a Packet to these, use {@link de.take_weiland.mods.commons.net.Packets#sendPacketToAllTracking Packets.sendPacketToAllTracking}
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<EntityPlayerMP> getTrackingPlayers(Entity entity) {
		if (!entity.worldObj.isRemote) {
			EntityTrackerEntry entry = (EntityTrackerEntry) MiscUtil.getReflector().getTrackerMap(((WorldServer) entity.worldObj).getEntityTracker()).lookup(entity.entityId);
			return entry == null ? Collections.emptySet() : entry.trackingPlayers;
		} else {
			return Collections.emptySet();
		}
	}
	
	public static ForgeDirection getFacing(Entity entity) {
		int dir = MathHelper.floor_double((entity.rotationYaw * 4 / 360) + 0.5) & 3;
		return ForgeDirection.VALID_DIRECTIONS[Direction.directionToFacing[dir]];
	}

}
