package de.take_weiland.mods.commons.internal.updater;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

import cpw.mods.fml.common.ModContainer;

public class ModsFolderMod implements UpdatableMod {

	private static final List<String> INTERNAL_MODS = Arrays.asList("mcp", "forge", "fml", "minecraft");
	
	private final UpdateController controller;
	private final ModContainer container;
	private URL updateURL;
	private ModUpdateState state = ModUpdateState.LOADING;
	private ModVersionInfo versionInfo;
	private File source;
	
	public ModsFolderMod(UpdateController controller, ModContainer container) {
		this.container = container;
		this.controller = controller;
		if (INTERNAL_MODS.contains(container.getModId().toLowerCase())) {
			transition(ModUpdateState.UNAVAILABLE);
			UpdateControllerLocal.LOGGER.info(String.format("Skipping FML-Internal mod %s", container.getModId()));
			return;
		}
		
		if ((updateURL = getUpdateURL(container)) == null) {
			transition(ModUpdateState.UNAVAILABLE);
			UpdateControllerLocal.LOGGER.info(String.format("Skipping mod %s with invalid or missing update URL", container.getModId()));
			return;
		}
		
		Object sourceObj = container.getMod() == null ? container : container.getMod();
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
			UpdateControllerLocal.LOGGER.warning(String.format("Cannot update mod %s because it's not a valid jar file!", container.getModId()));
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
	public ModContainer getContainer() {
		return container;
	}
	
	@Override
	public UpdateController getController() {
		return controller;
	}
	
	@Override
	public URL getUpdateURL() {
		return updateURL;
	}
	
	@Override
	public File getSource() {
		return source;
	}
	
	@Override
	public boolean transition(ModUpdateState desiredState) {
		boolean success;
		synchronized (this) {
			success = (state = state.transition(desiredState)) == desiredState;
		}
		if (success) {
			controller.onStateChange(this);
		}
		return success;
	}
	
	@Override
	public ModUpdateState getState() {
		synchronized (this) {
			return state;
		}
	}

	@Override
	public ModVersionInfo getVersionInfo() {
		return versionInfo;
	}

	@Override
	public void setVersionInfo(ModVersionInfo versionInfo) {
		this.versionInfo = versionInfo;
	}
}
