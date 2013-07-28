package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.ModContainer;

public abstract class ModContainerMod extends AbstractUpdatableMod {

	protected final ModContainer mod;

	public ModContainerMod(ModContainer mod, UpdateController controller) {
		super(controller, mod.getProcessedVersion());
		this.mod = mod;
	}

	@Override
	public String getModId() {
		return mod.getModId();
	}

	@Override
	public String getName() {
		return mod.getName();
	}
	
}
