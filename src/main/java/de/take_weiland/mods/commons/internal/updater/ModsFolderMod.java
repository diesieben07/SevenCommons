package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.InjectedModContainer;
import cpw.mods.fml.common.ModContainer;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class ModsFolderMod extends ModContainerMod {

	private URL updateURL;
	private File source;
	
	public ModsFolderMod(ModContainer mod, URL updateURL, UpdateController controller) {
		super(mod, controller);
		this.updateURL = updateURL;

		if (mod instanceof InjectedModContainer) {
			mod = ((InjectedModContainer) mod).wrappedContainer;
		}

		Object sourceObj = mod.getMod() == null ? mod : mod.getMod();
		URL sourceLoc = sourceObj.getClass().getProtectionDomain().getCodeSource().getLocation();
		
		UpdateControllerLocal.LOGGER.fine("Attempting to parse Mod-Zip URL for mod " + mod.getModId());
		UpdateControllerLocal.LOGGER.fine("URL is " + sourceLoc.toString());
		
		try {
			if (sourceLoc.getProtocol().equals("jar")) {
				JarURLConnection connection = (JarURLConnection) sourceLoc.openConnection();
				source = new File(connection.getJarFileURL().toURI());
			} else if (sourceLoc.getProtocol().equals("file")) {
				source = new File(sourceLoc.toURI());
			}
		} catch (IOException | URISyntaxException e) {
			exception(e, mod);
		}

		if (source == null) {
			UpdateControllerLocal.LOGGER.warning(String.format("Cannot update mod %s because it's not a valid jar file!", mod.getModId()));
		}
	}
	
	private static void exception(Exception e, ModContainer mod) {
		UpdateControllerLocal.LOGGER.warning(String.format("Exception during parsing Zip-URL for mod %s", mod.getModId()));
		e.printStackTrace();
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
