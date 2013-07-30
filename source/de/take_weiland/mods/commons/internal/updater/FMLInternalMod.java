package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

import cpw.mods.fml.common.ModContainer;

public class FMLInternalMod extends ModContainerMod {

	public FMLInternalMod(ModContainer mod, UpdateController controller) {
		super(mod, controller);
		transition(ModUpdateState.UNAVAILABLE);
	}

	@Override
	public URL getUpdateURL() {
		return null;
	}

	@Override
	public File getSource() {
		return null;
	}

}