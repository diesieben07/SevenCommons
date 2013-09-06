package de.take_weiland.mods.commons.templates;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityAbstract extends TileEntity {

	private boolean hasName = false;
	private String name;
	
	// for interface NameableTileEntity if implemented
	public final boolean hasCustomName() {
		return hasName;
	}
	
	public final boolean setCustomName(String name) {
		hasName = true;
		this.name = name;
		return true;
	}
	
	public final String getCustomName() {
		return name;
	}
	
}
