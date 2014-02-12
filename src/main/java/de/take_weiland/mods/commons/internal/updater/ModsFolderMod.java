package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Strings;
import cpw.mods.fml.common.ModContainer;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ModsFolderMod extends ModContainerMod {

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
		
		UpdateControllerLocal.LOGGER.fine("Attempting to parse Mod-Zip URL for mod " + mod.getModId());
		UpdateControllerLocal.LOGGER.fine("URL is " + sourceLoc.toString());
		
		try {
			if (sourceLoc.getProtocol().equals("jar")) {
				JarURLConnection connection = (JarURLConnection) sourceLoc.openConnection();
				source = new File(connection.getJarFileURL().toURI());
			}
		} catch (IOException e) {
			exception(e, mod);
		} catch (URISyntaxException e) {
			exception(e, mod);
		}
		
		if (source == null) {
			transition(ModUpdateState.UNAVAILABLE);
			UpdateControllerLocal.LOGGER.warning(String.format("Cannot update mod %s because it's not a valid jar file!", mod.getModId()));
		} else {
			transition(ModUpdateState.AVAILABLE);
		}
	}
	
	private static void exception(Exception e, ModContainer mod) {
		UpdateControllerLocal.LOGGER.warning(String.format("Exception during parsing Zip-URL for mod %s", mod.getModId()));
		e.printStackTrace();
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
