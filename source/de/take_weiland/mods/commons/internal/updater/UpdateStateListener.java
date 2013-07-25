package de.take_weiland.mods.commons.internal.updater;

public interface UpdateStateListener {

	public void onStateChange(UpdatableMod mod);
	
	public void onDownloadProgress(UpdatableMod mod, int progress, int total);
	
}
