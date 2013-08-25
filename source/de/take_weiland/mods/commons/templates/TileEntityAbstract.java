package de.take_weiland.mods.commons.templates;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityAbstract extends TileEntity {

	private boolean namingAllowed = true;
	private boolean hasName = false;
	private String name;
	
	protected final void disallowNaming() {
		namingAllowed = false;
	}
	
	public final boolean hasCustomName() {
		return hasName;
	}
	
	public final boolean setCustomName(String name) {
		if (!namingAllowed) {
			return false;
		}
		hasName = true;
		this.name = name;
		return true;
	}
	
	public final String getCustomName() {
		return name;
	}
	
}
