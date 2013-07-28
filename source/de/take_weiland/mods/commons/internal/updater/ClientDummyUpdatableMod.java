package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.URL;

public class ClientDummyUpdatableMod implements UpdatableMod {

	private UpdateController controller;
	private String modId;
	private String name;
	private ModUpdateState state = ModUpdateState.LOADING;
	private ModVersionCollection versions;
	
	private int downloadProgress = -1;
	private int downloadTotal;
	
	@Override
	public String getModId() {
		return modId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UpdateController getController() {
		return controller;
	}

	public void setController(UpdateController controller) {
		this.controller = controller;
	}

	public void setModId(String modId) {
		this.modId = modId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setState(ModUpdateState state) {
		this.state = state;
	}

	@Override
	public URL getUpdateURL() {
		return null;
	}

	@Override
	public File getSource() {
		return null;
	}

	@Override
	public boolean transition(ModUpdateState desiredState) {
		state = desiredState;
		return true;
	}

	@Override
	public ModUpdateState getState() {
		return state;
	}

	@Override
	public ModVersionCollection getVersions() {
		return versions;
	}

	public void setVersions(ModVersionCollection versions) {
		this.versions = versions;
	}

	@Override
	public void setDownloadProgress(int progress, int total) {
		if (total == progress) {
			progress = -1;
		}
		this.downloadProgress = progress;
		this.downloadTotal = total;
	}

	@Override
	public int getDowloadProgress(int max) {
		if (downloadProgress < 0) {
			return -1;
		} else {
			return (int) (max / (float)downloadTotal * downloadProgress);
		}
	}

}
