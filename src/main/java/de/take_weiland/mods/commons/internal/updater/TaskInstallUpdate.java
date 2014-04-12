package de.take_weiland.mods.commons.internal.updater;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher;
import de.take_weiland.mods.commons.util.Scheduler;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class TaskInstallUpdate implements Runnable {

	private final UpdateControllerLocal controller;
	private final UpdatableMod mod;
	private final ModVersion version;

	public TaskInstallUpdate(UpdateControllerLocal controller, UpdatableMod mod, ModVersion version) {
		this.controller = controller;
		this.mod = mod;
		this.version = version;
	}

	@Override
	public void run() {
		boolean success = false;
		try {
			URL downloadURL = new URL(version.getDownloadURL());
			String newFileName = downloadURL.getFile();

			URLConnection conn;
			ReadableByteChannel in = null;
			FileChannel out = null;
			long len = -1;
			try {
				conn = downloadURL.openConnection();
				len = conn.getContentLengthLong();
				if (len > 0) {
					controller.modifyPendingBytes(len);
				}
				String contDisp = conn.getHeaderField("Content-Disposition");
				if (contDisp != null) {
					int filenameIdx = contDisp.toLowerCase().indexOf("filename=");
					if (filenameIdx >= 0) {
						String filename = contDisp.split("filename=")[1].trim();
						if (filename.length() > 0) {
							newFileName = filename;
						}
					}
				}

				if (!newFileName.endsWith(".jar") && !newFileName.endsWith(".zip")) {
					newFileName += ".jar";
				}

				if (mod.getSource() == null) {
					throw new IOException("ModSource is null!");
				}
				File newModFile = new File(mod.getSource().getParentFile() + newFileName);
				String absPath = newModFile.getAbsolutePath();
				absPath = absPath.substring(0, absPath.length() - 4);
				int i = 0;
				while (newModFile.exists()) {
					++i;
					newModFile = new File(absPath + i + ".jar");
				}
				controller.registerForFailureDeletion(newModFile.toPath());

				System.out.println("Saving update for " + mod.getName() + " to " + newModFile.getAbsolutePath());

				in = new MonitoringByteChannel(Channels.newChannel(conn.getInputStream()), controller);
				out = new FileOutputStream(newModFile).getChannel();

				ByteStreams.copy(in, out);

				File marker = new File(mod.getSource().getPath() + MinecraftRelauncher.UPDATE_MARKER_POSTFIX);
				controller.registerForFailureDeletion(marker.toPath());

				if (mod.getModId().equals("testmod_sc")) throw new IOException("test");

				Files.touch(marker);
				success = true;
			} catch (IOException e) {
				UpdateControllerLocal.LOGGER.warning(String.format("IOException during update download for mod %s", mod.getModId()));
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		} catch (MalformedURLException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Failed to download update for mod %s, the download URL is invalid", mod.getModId()));
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			final boolean succFinal = success;
			Scheduler.forEnvironment().execute(new Runnable() {
				@Override
				public void run() {
					mod.transition(succFinal ? ModUpdateState.INSTALL_OK : ModUpdateState.INSTALL_FAIL);
				}
			});
		}
	}
}
