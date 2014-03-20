package de.take_weiland.mods.commons.internal.updater;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerUpdateInformation implements IExtendedEntityProperties {

	public static final String IDENTIFIER = "SevenCommonsUpdater";

	private EntityPlayer player;

	@Override
	public void saveNBTData(NBTTagCompound compound) { }

	@Override
	public void loadNBTData(NBTTagCompound compound) { }

	@Override
	public void init(Entity entity, World world) {
		player = (EntityPlayer)entity;
	}

}
