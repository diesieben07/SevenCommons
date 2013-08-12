package de.take_weiland.mods.commons.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public final class Entities {

	private Entities() { }

	public static final NBTTagCompound getData(Entity entity) {
		if (entity instanceof EntityPlayer) {
			return NBT.getOrCreateCompound(entity.getEntityData(), EntityPlayer.PERSISTED_NBT_TAG);
		} else {
			return entity.getEntityData();
		}
	}

	public static final NBTTagCompound getData(Entity entity, String subKey) {
		return NBT.getOrCreateCompound(getData(entity), subKey);
	}

}
