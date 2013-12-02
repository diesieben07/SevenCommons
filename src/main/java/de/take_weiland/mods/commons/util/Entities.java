package de.take_weiland.mods.commons.util;

import java.util.Collections;
import java.util.Set;

import de.take_weiland.mods.commons.network.Packets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

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
	 * If you want to send a Packet to these, use {@link Packets#sendPacketToAllTracking}
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<EntityPlayerMP> getTrackingPlayers(Entity entity) {
		if (Sides.logical(entity).isServer()) {
			EntityTrackerEntry entry = (EntityTrackerEntry) ((EntityTrackerProxy)((WorldServer)entity.worldObj).getEntityTracker()).getTrackerMap().lookup(entity.entityId);
			return entry == null ? Collections.emptySet() : entry.trackingPlayers;
		} else {
			return Collections.emptySet();
		}
	}

}
