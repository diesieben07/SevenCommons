package de.take_weiland.mods.commons.internal.updater.tasks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import de.take_weiland.mods.commons.internal.updater.InvalidModVersionException;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection.ModVersion;
import de.take_weiland.mods.commons.internal.updater.UpdateControllerLocal;

public class SearchUpdates implements Runnable {

	private UpdatableMod mod;
	
	public SearchUpdates(UpdatableMod mod) {
		this.mod = mod;
	}

	@Override
	public void run() {
		URL url = mod.getUpdateURL();
		try (Reader reader = new InputStreamReader(url.openStream())) {
				
			ModVersionCollection versionInfo = ModVersionCollection.create(reader, mod.getContainer());
			
			mod.setVersionInfo(versionInfo);
			
			ModVersion newestInstallable = versionInfo.getNewestInstallableVersion();
			ModVersion newest = versionInfo.getNewestVersion();
			
			if (newestInstallable != null && newestInstallable.compareTo(versionInfo.getCurrentVersion()) > 0) {
				if (mod.transition(ModUpdateState.UPDATES_AVAILABLE)) {
					UpdateControllerLocal.LOGGER.info("Updates available for mod " + mod.getContainer().getModId());
				}
			} else if (newest != null && newest.compareTo(versionInfo.getCurrentVersion()) > 0) {
				if (mod.transition(ModUpdateState.MINECRAFT_OUTDATED)) {
					UpdateControllerLocal.LOGGER.info("Cannot update mod " + mod.getContainer().getModId() + " because Minecraft is outdated.");
				}
			} else {
				if (mod.transition(ModUpdateState.UP_TO_DATE)) {
					UpdateControllerLocal.LOGGER.info("Mod " + mod.getContainer().getModId() + " is up to date.");
				}
			}
			
		} catch (IOException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("IOException during update checking for mod %s", mod.getContainer().getModId()));
			mod.transition(ModUpdateState.CHECKING_FAILED);
		} catch (InvalidModVersionException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Version Info-File for mod %s is invalid", mod.getContainer().getModId()));
			mod.transition(ModUpdateState.CHECKING_FAILED);
		}
	}	
}