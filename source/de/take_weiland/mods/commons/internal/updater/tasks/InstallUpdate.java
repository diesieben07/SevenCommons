package de.take_weiland.mods.commons.internal.updater.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import com.google.common.io.ByteStreams;

import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersion;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection;
import de.take_weiland.mods.commons.internal.updater.MonitoringByteChannel;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateControllerLocal;

public class InstallUpdate implements Runnable {

	private UpdatableMod mod;
	private ModVersion version;
	
	public InstallUpdate(UpdatableMod mod, ModVersion version) {
		this.mod = mod;
		ModVersionCollection info = mod.getVersions();
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
				UpdateControllerLocal.LOGGER.warning(String.format("IOException during update download for mod %s", mod.getContainer().getModId()));
				mod.transition(ModUpdateState.DOWNLOAD_FAILED);
			}
		} catch (MalformedURLException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Failed to download update for mod %s, the download URL is invalid", mod.getContainer().getModId()));
			mod.transition(ModUpdateState.DOWNLOAD_FAILED);
		}
	}
}