package de.take_weiland.mods.commons.updater;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Strings;

import cpw.mods.fml.common.ModContainer;

public class UpdatableMod {

	private final ModContainer container;
	private final URL updateURL;
	private transient ModUpdateState state = ModUpdateState.LOADING;
	
	public UpdatableMod(ModContainer container) {
		this.container = container;
		updateURL = getUpdateURL(container);
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
	
	public ModContainer getContainer() {
		return container;
	}
	
	public URL getUpdateURL() {
		return updateURL;
	}
	
	public boolean transition(ModUpdateState desiredState) {
		synchronized (this) {
			return (state = state.transition(desiredState)) == desiredState;
		}
	}
	
	public ModUpdateState getState() {
		synchronized (this) {
			return state;
		}
	}
}
