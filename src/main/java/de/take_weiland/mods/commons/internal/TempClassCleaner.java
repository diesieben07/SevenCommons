package de.take_weiland.mods.commons.internal;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author diesieben07
 */
public class TempClassCleaner {

	public static void main(String[] args) throws IOException {
		File[] dirs = new File("./eclipse/").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("CLASSLOADER_TEMP") || name.startsWith("sevencommonsdyn");
			}
		});
		for (File dir : dirs) {
			FileUtils.deleteDirectory(dir);
		}
	}

}
