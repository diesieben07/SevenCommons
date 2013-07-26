package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.base.Strings;

import cpw.mods.fml.common.ModContainer;

public class ModsFolderMod extends AbstractUpdatableMod {

	private URL updateURL;
	private File source;
	
	public ModsFolderMod(ModContainer mod, UpdateController controller) {
		super(mod, controller);
		
		if ((updateURL = getUpdateURL(mod)) == null) {
			transition(ModUpdateState.UNAVAILABLE);
			UpdateControllerLocal.LOGGER.info(String.format("Skipping mod %s with invalid or missing update URL", mod.getModId()));
			return;
		}
		
		Object sourceObj = mod.getMod() == null ? mod : mod.getMod();
		URL sourceLoc = sourceObj.getClass().getProtectionDomain().getCodeSource().getLocation();
		
		try {
			File file = new File(sourceLoc.toURI());
			
			if (file.isFile() && (file.getPath().endsWith(".jar") || file.getPath().endsWith(".zip"))) {
				source = file;
			}
		} catch (URISyntaxException e) { // ok, no jar source file
		}
		
		if (source == null) {
			transition(ModUpdateState.UNAVAILABLE);
			UpdateControllerLocal.LOGGER.warning(String.format("Cannot update mod %s because it's not a valid jar file!", mod.getModId()));
		} else {
			transition(ModUpdateState.AVAILABLE);
		}
	}
	
	private static URL getUpdateURL(ModContainer mc) {
		String url = mc.getMetadata().updateUrl;
		if (Strings.isNullOrEmpty(url)) {
			return null;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	@Override
	public URL getUpdateURL() {
		return updateURL;
	}
	
	@Override
	public File getSource() {
		return source;
	}
}
