package de.take_weiland.mods.commons.updater;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.updater.ModVersionInfo.ModVersion;

public class UpdateController {

	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	static final Logger LOGGER;
	
	static {
		FMLLog.makeLog(LOG_CHANNEL);
		LOGGER = Logger.getLogger(LOG_CHANNEL);
	}

	private ListeningExecutorService executor;
	
	private final Map<ModContainer, UpdatableMod> mods;
	
	public UpdateController() {
		mods = Maps.toMap(Loader.instance().getActiveModList(), new Function<ModContainer, UpdatableMod>() {
			
			public UpdatableMod apply(ModContainer mod) {
				return new UpdatableMod(mod);
			}
			
		});
		executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build()));
	}
	
	public void searchForUpdates(ModContainer modContainer) {
		UpdatableMod mod = mods.get(modContainer);
		if (mod.transition(ModUpdateState.CHECKING)) {
			executor.execute(new SearchUpdates(mod));
		}
	}
	
	private static class SearchUpdates implements Runnable {

		private UpdatableMod mod;
		
		SearchUpdates(UpdatableMod mod) {
			this.mod = mod;
		}

		@Override
		public void run() {
			URL url = mod.getUpdateURL();
			if (url == null) {
				mod.transition(ModUpdateState.UNAVAILABLE);
			} else {
				try (Reader reader = new InputStreamReader(url.openStream())) {
					
					ModVersionInfo versionInfo = ModVersionInfo.create(reader, mod.getContainer());
					
					ModVersion newestInstallable = versionInfo.getNewestInstallableVersion();
					ModVersion newest = versionInfo.getNewestVersion();
					
					if (newestInstallable != null && newestInstallable.compareTo(versionInfo.getCurrentVersion()) > 0) {
						mod.transition(ModUpdateState.UPDATES_AVAILABLE);
					} else if (newest != null && newest.compareTo(versionInfo.getCurrentVersion()) > 0) {
						mod.transition(ModUpdateState.MINECRAFT_OUTDATED);
					} else {
						mod.transition(ModUpdateState.UP_TO_DATE);
					}
					
				} catch (IOException e) {
					LOGGER.warning(String.format("IOException during update checking for mod %s", mod.getContainer().getModId()));
					mod.transition(ModUpdateState.CHECKING_FAILED);
				} catch (InvalidModVersionException e) {
					LOGGER.warning(String.format("Version Info-File for mod %s is invalid", mod.getContainer().getModId()));
					mod.transition(ModUpdateState.CHECKING_FAILED);
				}
			}
		}
		
	}
	
	private static class DownloadMod implements Runnable {

		private UpdatableMod mod;
		
		DownloadMod(UpdatableMod mod) {
			this.mod = mod;
		}
		
		@Override
		public void run() {
			
		}
		
	}
}
