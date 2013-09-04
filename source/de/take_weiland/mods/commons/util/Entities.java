package de.take_weiland.mods.commons.util;

import java.util.Collections;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;

public final class Entities {

	private Entities() { }

	public static NBTTagCompound getData(Entity entity) {
		if (entity instanceof EntityPlayer) {
			return NBT.getOrCreateCompound(entity.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG);
		} else {
			return entity.getEntityData();
		}
	}

	public static NBTTagCompound getData(Entity entity, String subKey) {
		return NBT.getOrCreateCompound(getData(entity), subKey);
	}
	
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
