package de.take_weiland.mods.commons.internal.exclude;

import cpw.mods.fml.relauncher.IFMLCallHook;
import de.take_weiland.mods.commons.internal.mcrestarter.MinecraftRelauncher;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class SCCallHook implements IFMLCallHook {

	private File mcDir;

	@Override
	public Void call() {
		File modsFolder = new File(mcDir, "mods");
		File tempDataFile = new File(modsFolder, MinecraftRelauncher.UPDATE_INFO_FILE);
		if (tempDataFile.exists()) {
			try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(tempDataFile)))) {
				File jarFile = new File(in.readUTF());
				Files.delete(jarFile.toPath());
			} catch (IOException e) {
				displayError(e);
			}
			try {
				Files.delete(tempDataFile.toPath());
			} catch (IOException e) {
				displayError(e);
			}
		}

		return null;
	}

	private void displayError(Exception e) {
		System.err.println("[SevenCommons] Warning! Couldn't read/delete temporary updater file!");
		e.printStackTrace();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		mcDir = (File) data.get("mcLocation");
	}
	
}
