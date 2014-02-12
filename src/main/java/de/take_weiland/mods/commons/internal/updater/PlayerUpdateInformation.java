package de.take_weiland.mods.commons.internal.updater;

import de.take_weiland.mods.commons.internal.PacketDownloadProgress;
import de.take_weiland.mods.commons.internal.PacketModState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerUpdateInformation implements IExtendedEntityProperties, UpdateStateListener {

	public static final String IDENTIFIER = "SevenCommonsUpdater";

	private EntityPlayer player;
	private int oldDownloadProgress = -2; // -1 is taken for "no progress"
	
	@Override
	public void saveNBTData(NBTTagCompound compound) { }

	@Override
	public void loadNBTData(NBTTagCompound compound) { }

	@Override
	public void init(Entity entity, World world) {
		player = (EntityPlayer)entity;
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		new PacketModState(mod, mod.getState()).sendTo(player);
	}

	@Override
	public void onDownloadProgress(UpdatableMod mod) {
		int progress = mod.getDowloadProgress(100);
		if (oldDownloadProgress != progress) {
			oldDownloadProgress = progress;
			new PacketDownloadProgress(mod, progress).sendTo(player);
		}
	}

}
