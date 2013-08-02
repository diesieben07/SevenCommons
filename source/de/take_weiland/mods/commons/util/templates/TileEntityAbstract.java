package de.take_weiland.mods.commons.util.templates;

import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityAbstract<C extends TileEntityAbstract<C>> extends TileEntity {

	private boolean namingAllowed = true;
	private boolean hasName = false;
	private String name;
	
	@SuppressWarnings("unchecked")
	protected final C disallowNaming() {
		namingAllowed = false;
		return (C) this;
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
