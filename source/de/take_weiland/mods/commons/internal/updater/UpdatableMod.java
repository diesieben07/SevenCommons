package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

import cpw.mods.fml.common.ModContainer;

public interface UpdatableMod {

	public ModContainer getContainer();

	public UpdateController getController();

	public URL getUpdateURL();

	public File getSource();

	public boolean transition(ModUpdateState desiredState);

	public ModUpdateState getState();

	public ModVersionCollection getVersions();
	
	public void setDownloadProgress(int progress, int total);
	
	public int getDowloadProgress(int max);
	
}
