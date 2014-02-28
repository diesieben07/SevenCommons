package de.take_weiland.mods.commons.internal.updater;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import de.take_weiland.mods.commons.internal.exclude.SCCallHook;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class TaskInstallUpdate implements Runnable {

	private UpdatableMod mod;
	private ModVersion version;

	public TaskInstallUpdate(UpdatableMod mod, ModVersion version) {
		this.mod = mod;
		ModVersionCollection info = mod.getVersions();
		if (info == null || !info.getInstallableVersions().contains(version)) {
			throw new IllegalArgumentException(String.format("Version %s is not available for mod %s", version.modVersion, mod.getModId()));
		}
		this.version = version;
	}

	@Override
	public void run() {
		try {
			URL downloadURL = new URL(version.downloadURL);
			String newFileName = downloadURL.getFile();
			if (!newFileName.endsWith(".zip") && !newFileName.endsWith(".jar")) {
				UpdateControllerLocal.LOGGER.warning(String.format("Failed to download update for mod %s, the download URL is not a jar or zip file!", mod.getModId()));
				mod.transition(ModUpdateState.DOWNLOAD_FAILED);
				return;
			}

			File newModFile = new File(mod.getSource().getParentFile() + newFileName);
			URLConnection conn;
			ReadableByteChannel in = null;
			FileChannel out = null;
			try {
				conn = downloadURL.openConnection();

				in = new MonitoringByteChannel(Channels.newChannel(conn.getInputStream()), mod, conn.getContentLength());
				out = new FileOutputStream(newModFile).getChannel();

				ByteStreams.copy(in, out);

				Files.touch(new File(mod.getSource().getPath() + SCCallHook.UPDATE_MARKER_POSTFIX));

				mod.transition(ModUpdateState.PENDING_RESTART);
			} catch (IOException e) {
				UpdateControllerLocal.LOGGER.warning(String.format("IOException during update download for mod %s", mod.getModId()));
				e.printStackTrace();
				mod.transition(ModUpdateState.DOWNLOAD_FAILED);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		} catch (MalformedURLException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Failed to download update for mod %s, the download URL is invalid", mod.getModId()));
			mod.transition(ModUpdateState.DOWNLOAD_FAILED);
		}
	}
}
