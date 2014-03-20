package de.take_weiland.mods.commons.internal.updater;

import java.util.Collection;

public interface UpdateController {

	/**
	 * search for updates on all mods
	 */
	public void searchForUpdates();
	
	/**
	 * 
	 * @return
	 */
	public Collection<? extends UpdatableMod> getMods();
	
	public UpdatableMod getMod(String modId);
	
	/**
	 * search for updates on the given mod
	 * @param mod the mod
	 */
	public void searchForUpdates(UpdatableMod mod);

	void update(UpdatableMod mod);

	boolean optimizeVersionSelection();

	void performInstall();

	boolean isRefreshing();

	boolean isSelectionValid();

	boolean isSelectionOptimized();

	public boolean restartMinecraft();

	void onStateChange(UpdatableMod mod, ModUpdateState oldState);
}
