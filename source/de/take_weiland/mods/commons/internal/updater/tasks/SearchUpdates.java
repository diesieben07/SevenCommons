package de.take_weiland.mods.commons.internal.updater.tasks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import de.take_weiland.mods.commons.internal.updater.InvalidModVersionException;
import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
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
			ModVersionCollection versions = mod.getVersions();
			
			versions.injectAvailableVersions(SearchUpdates.parseVersionFile(mod, reader));
			
			ModVersion newestInstallable = versions.getNewestInstallableVersion();
			ModVersion newest = versions.getNewestVersion();
			
			if (newestInstallable != null && newestInstallable.modVersion.compareTo(versions.getCurrentVersion().modVersion) > 0) {
				if (mod.transition(ModUpdateState.UPDATES_AVAILABLE)) {
					UpdateControllerLocal.LOGGER.info("Updates available for mod " + mod.getModId());
				}
			} else if (newest != null && newest.modVersion.compareTo(versions.getCurrentVersion().modVersion) > 0) {
				if (mod.transition(ModUpdateState.MINECRAFT_OUTDATED)) {
					UpdateControllerLocal.LOGGER.info("Cannot update mod " + mod.getModId() + " because Minecraft is outdated.");
				}
			} else {
				if (mod.transition(ModUpdateState.UP_TO_DATE)) {
					UpdateControllerLocal.LOGGER.info("Mod " + mod.getModId() + " is up to date.");
				}
			}
			
		} catch (IOException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("IOException during update checking for mod %s", mod.getModId()));
			mod.transition(ModUpdateState.CHECKING_FAILED);
		} catch (InvalidModVersionException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Version Info-File for mod %s is invalid", mod.getModId()));
			mod.transition(ModUpdateState.CHECKING_FAILED);
		}
	}
	
	private  static final String[] JSON_KEYS = new String[] {
		"version",	"minecraftVersion", "url"		
	};
	
	private static final JdomParser JSON_PARSER = new JdomParser();

	private  static final List<ModVersion> parseVersionFile(UpdatableMod mod, Reader reader) throws InvalidModVersionException {
		try {
			ImmutableList.Builder<ModVersion> versions = ImmutableList.builder();
			JsonRootNode json = JSON_PARSER.parse(reader);
			
			if (!json.isArrayNode()) {
				invalid();
			}
			
			for (JsonNode versionNode : json.getElements()) {
				for (String key : JSON_KEYS) {
					if (!versionNode.isStringValue(key)) {
						invalid();
					}
				}
				
				String modVersion = versionNode.getStringValue("version");
				String minecraftVersion = versionNode.getStringValue("minecraftVersion");
				String url = versionNode.getStringValue("url");
				String patchNotes = versionNode.isStringValue("patchNotes") ? versionNode.getStringValue("patchNotes") : null;
				versions.add(new ModVersion(mod, new DefaultArtifactVersion(modVersion), minecraftVersion, url, patchNotes));
			}
			return versions.build();
		} catch (Throwable t) {
			t.printStackTrace();
			Throwables.propagateIfPossible(t);
			return invalid(t);
		}
	}
	
	private static final void invalid() throws InvalidModVersionException {
		throw new InvalidModVersionException("Failed to parse ModVersionInfo");
	}
	
	private static final List<ModVersion> invalid(Throwable t) throws InvalidModVersionException {
		throw new InvalidModVersionException("Failed to parse ModVersionInfo", t);
	}
}