package de.take_weiland.mods.commons.templates;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractTileEntity extends TileEntity implements NameableTileEntity {

	private static final String CUSTOM_NAME_KEY = "customName";
	
	private boolean hasName = false;
	private String name;
	
	@Override
	public boolean hasCustomName() {
		return hasName;
	}
	
	@Override
	public boolean setCustomName(String name) {
		hasName = true;
		this.name = name;
		return true;
	}
	
	@Override
	public String getCustomName() {
		return name;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		if (hasCustomName()) {
			nbt.setString(CUSTOM_NAME_KEY, getCustomName());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey(CUSTOM_NAME_KEY)) {
			setCustomName(nbt.getString(CUSTOM_NAME_KEY));
		}
	}

}
