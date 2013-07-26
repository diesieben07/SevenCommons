package de.take_weiland.mods.commons.internal.updater;

import java.util.List;

public interface UpdateController {

	/**
	 * search for updates on all mods
	 */
	public void searchForUpdates();
	
	/**
	 * 
	 * @return
	 */
	public List<UpdatableMod> getMods();
	
	/**
	 * search for updates on the given mod
	 * @param modContainer the mod
	 */
	public void searchForUpdates(UpdatableMod mod);

	/**
	 * update the given mod to the given version
	 * @param mod
	 * @param version
	 */
	public void update(UpdatableMod mod, ModVersion version);
	
	public void registerListener(UpdateStateListener listener);
	
	public void unregisterListener(UpdateStateListener listener);
	
	public void onStateChange(UpdatableMod mod);
	
}
