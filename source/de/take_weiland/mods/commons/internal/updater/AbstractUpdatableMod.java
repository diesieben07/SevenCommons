package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.ModContainer;

public abstract class AbstractUpdatableMod implements UpdatableMod {

	private final Object stateLock = new Object();
	private final Object downloadLock = new Object();
	
	protected final ModContainer mod;
	protected final UpdateController controller;
	private final ModVersionCollection versions;
	
	private ModUpdateState state = ModUpdateState.LOADING;
	private int downloadProgress = -1;
	private int downloadTotal;

	public AbstractUpdatableMod(ModContainer mod, UpdateController controller) {
		this.mod = mod;
		this.controller = controller;
		versions = new ModVersionCollection(mod);
	}

	@Override
	public final ModContainer getContainer() {
		return mod;
	}

	@Override
	public final UpdateController getController() {
		return controller;
	}

	@Override
	public boolean transition(ModUpdateState desiredState) {
		boolean success;
		synchronized (stateLock) {
			success = (state = state.transition(desiredState)) == desiredState;
		}
		if (success) {
			controller.onStateChange(this);
		}
		return success;
	}

	@Override
	public ModUpdateState getState() {
		synchronized (stateLock) {
			return state;
		}
	}

	@Override
	public ModVersionCollection getVersions() {
		return versions;
	}

	@Override
	public void setDownloadProgress(int progress, int total) {
		if (progress == total) {
			progress = -1;
		}
		synchronized (downloadLock) {
			this.downloadProgress = progress;
			this.downloadTotal = total;
		}
	}

	@Override
	public int getDowloadProgress(int max) {
		synchronized (downloadLock) {
			if (downloadProgress < 0) {
				return -1;
			} else {
				return (int) (max / (float)downloadTotal * downloadProgress);
			}
		}
	}
}