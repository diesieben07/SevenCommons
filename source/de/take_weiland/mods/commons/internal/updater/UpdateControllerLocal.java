package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.updater.ModVersionInfo.ModVersion;

public class UpdateControllerLocal implements UpdateController {

	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	static final Logger LOGGER;
	
	static {
		FMLLog.makeLog(LOG_CHANNEL);
		LOGGER = Logger.getLogger(LOG_CHANNEL);
	}

	private ListeningExecutorService executor;

	private final Set<UpdateStateListener> listeners = Sets.newHashSet();
	private final Map<ModContainer, UpdatableMod> mods;
	
	public UpdateControllerLocal() {
		mods = Maps.toMap(Loader.instance().getActiveModList(), new Function<ModContainer, UpdatableMod>() {
			
			public UpdatableMod apply(ModContainer mod) {
				return new UpdatableMod(UpdateControllerLocal.this, mod);
			}
			
		});
		executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build()));
	}
	
	@Override
	public UpdatableMod getMod(ModContainer modContainer) {
		UpdatableMod mod = mods.get(modContainer);
		if (mod == null) {
			throw new IllegalArgumentException(String.format("Mod %s, hasn't been registered to the UpdateController!", modContainer.getModId()));
		}
		return mod;
	}
	
	@Override
	public Collection<UpdatableMod> getMods() {
		return mods.values();
	}
	
	@Override
	public void searchForUpdates() {
		for (UpdatableMod mod : mods.values()) {
			searchForUpdates(mod);
		}
	}
	
	@Override
	public void searchForUpdates(UpdatableMod mod) {
		validate(mod);
		if (mod.transition(ModUpdateState.CHECKING)) {
			executor.execute(new SearchUpdates(mod));
		}
	}
	
	@Override
	public void update(UpdatableMod mod, ModVersion version) {
		validate(mod);
		if (mod.transition(ModUpdateState.DOWNLOADING)) {
			executor.execute(new InstallUpdate(mod, version));
		}
	}
	
	private void validate(UpdatableMod mod) {
		if (!mods.containsKey(mod.getContainer())) { // check for the container here since key searching is faster
			throw new IllegalArgumentException(String.format("Mod %s not valid for this UpdateController!", mod.getContainer().getModId()));
		}
	}
	
	private class SearchUpdates implements Runnable {

		private UpdatableMod mod;
		
		SearchUpdates(UpdatableMod mod) {
			this.mod = mod;
		}

		@Override
		public void run() {
			URL url = mod.getUpdateURL();
			try (Reader reader = new InputStreamReader(url.openStream())) {
					
				ModVersionInfo versionInfo = ModVersionInfo.create(reader, mod.getContainer());
				
				mod.setVersionInfo(versionInfo);
				
				ModVersion newestInstallable = versionInfo.getNewestInstallableVersion();
				ModVersion newest = versionInfo.getNewestVersion();
				
				if (newestInstallable != null && newestInstallable.compareTo(versionInfo.getCurrentVersion()) > 0) {
					if (mod.transition(ModUpdateState.UPDATES_AVAILABLE)) {
						LOGGER.info("Updates available for mod " + mod.getContainer().getModId());
					}
				} else if (newest != null && newest.compareTo(versionInfo.getCurrentVersion()) > 0) {
					if (mod.transition(ModUpdateState.MINECRAFT_OUTDATED)) {
						LOGGER.info("Cannot update mod " + mod.getContainer().getModId() + " because Minecraft is outdated.");
					}
				} else {
					if (mod.transition(ModUpdateState.UP_TO_DATE)) {
						LOGGER.info("Mod " + mod.getContainer().getModId() + " is up to date.");
					}
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
	
	private static class InstallUpdate implements Runnable {

		private UpdatableMod mod;
		private ModVersion version;
		
		InstallUpdate(UpdatableMod mod, ModVersion version) {
			this.mod = mod;
			ModVersionInfo info = mod.getVersionInfo();
			if (info == null || !info.getInstallableVersions().contains(version)) {
				throw new IllegalArgumentException(String.format("Version %s is not available for mod %s", version.modVersion, mod.getContainer().getModId()));
			}
			this.version = version;
		}
		
		@Override
		public void run() {
			try {
				URL downloadURL = new URL(version.downloadURL);
				
				File tempFile = new File(mod.getSource().getAbsolutePath() + ".7update");
				URLConnection conn;
				try {
					conn = downloadURL.openConnection();
					
					try (ReadableByteChannel in = new MonitoringByteChannel(Channels.newChannel(conn.getInputStream()), mod, conn.getContentLength());
							FileChannel out = new FileOutputStream(tempFile).getChannel();) {
						
						ByteStreams.copy(in, out);
						
						mod.transition(ModUpdateState.PENDING_RESTART);
						
					}
					
				} catch (IOException e) {
					LOGGER.warning(String.format("IOException during update download for mod %s", mod.getContainer().getModId()));
					mod.transition(ModUpdateState.DOWNLOAD_FAILED);
				}
				
				
			} catch (MalformedURLException e) {
				LOGGER.warning(String.format("Failed to download update for mod %s, the download URL is invalid", mod.getContainer().getModId()));
				mod.transition(ModUpdateState.DOWNLOAD_FAILED);
			}
		}
	}

	@Override
	public void registerListener(UpdateStateListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void unregisterListener(UpdateStateListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		synchronized (listeners) {
			for (UpdateStateListener listener : listeners) {
				listener.onStateChange(mod);
			}
		}
	}
	
	@Override
	public void onUpdateProgress(UpdatableMod mod, int progress, int total) {
		synchronized (listeners) {
			for (UpdateStateListener listener : listeners) {
				listener.onDownloadProgress(mod, progress, total);
			}
		}
	}
}
