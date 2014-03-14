package de.take_weiland.mods.commons.internal.exclude;

import cpw.mods.fml.relauncher.IFMLCallHook;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.updater.UpdateControllerLocal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class SCCallHook implements IFMLCallHook {

	public static final String UPDATE_MARKER_POSTFIX = "._sc_updated";
	public static final String BACKUP_POSTFIX = ".backup";
	
	private static final FileFilter UPATED_MODS_FILTER = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			if (!pathname.getPath().endsWith(".jar") && !pathname.getPath().endsWith(".zip")) {
				return false;
			}
			File marker = new File(pathname.getPath() + UPDATE_MARKER_POSTFIX);
			return marker.exists() && marker.isFile() && marker.canRead();

		}
	};
	
	private File mcDir;
	
	@Override
	public Void call() {
		for (File modFolder : getModFolders()) {
			File[] updatableMods = modFolder.listFiles(UPATED_MODS_FILTER);
			if (updatableMods == null) {
				continue;
			}
			for (File mod : updatableMods) {
				try {
					File backup = new File(mod.getPath() + BACKUP_POSTFIX);
					Files.move(mod.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

					File marker = new File(mod.getPath() + UPDATE_MARKER_POSTFIX);
					Files.delete(marker.toPath());

				} catch (IOException e) {
					e.printStackTrace();
					UpdateControllerLocal.LOGGER.warning(String.format("IOException during final update for mod %s", mod.getName()));
				}
			}
		}
		
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		mcDir = (File) data.get("mcLocation");
	}
	
	private File[] getModFolders() {
		File modsDir = new File(mcDir, "mods");
		return new File[] {
			modsDir,
			new File(modsDir, SevenCommons.MINECRAFT_VERSION)
		};
	}

}
