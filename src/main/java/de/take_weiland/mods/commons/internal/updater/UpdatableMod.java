package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

public interface UpdatableMod {

	public String getModId();
	
	public String getName();

	public UpdateController getController();

	public URL getUpdateURL();

	public File getSource();

	public ModVersionCollection getVersions();

	ModUpdateState getState();

	boolean transition(ModUpdateState state);
	
}
