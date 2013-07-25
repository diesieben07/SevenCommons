package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

import cpw.mods.fml.common.ModContainer;

public interface UpdatableMod {

	public abstract ModContainer getContainer();

	public abstract UpdateController getController();

	public abstract URL getUpdateURL();

	public abstract File getSource();

	public abstract boolean transition(ModUpdateState desiredState);

	public abstract ModUpdateState getState();

	public abstract ModVersionCollection getVersions();

	public abstract void setVersionInfo(ModVersionCollection versionInfo);

}