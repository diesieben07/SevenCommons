package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.ModContainer;

import java.io.File;
import java.net.URL;

public class FMLInternalMod extends ModContainerMod {

	public FMLInternalMod(ModContainer mod, UpdateController controller) {
		super(mod, controller);
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
