package de.take_weiland.mods.commons.internal.updater;

import java.util.Collection;

import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.updater.ModVersionInfo.ModVersion;

public interface UpdateController {

	/**
	 * search for updates on all mods
	 */
	public void searchForUpdates();
	
	public Collection<ModsFolderMod> getMods();
	
	public UpdatableMod getMod(ModContainer mod);

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
	
	public void onUpdateProgress(UpdatableMod mod, int progress, int total);
}