package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public abstract class AbstractUpdateController implements UpdateController {

	protected static final Function<UpdatableMod, String> ID_RETRIEVER = new Function<UpdatableMod, String>() {
		
		@Override
		public String apply(UpdatableMod mod) {
			return mod.getModId();
		}
		
	};
	
	private final Set<UpdateStateListener> stateListeners = Sets.newHashSet();
	protected Map<String, ? extends UpdatableMod> mods = Collections.emptyMap();

	@Override
	public Collection<? extends UpdatableMod> getMods() {
		return mods.values();
	}

	@Override
	public UpdatableMod getMod(String modId) {
		return mods.get(modId);
	}

	@Override
	public void searchForUpdates() {
		for (UpdatableMod mod : mods.values()) {
			searchForUpdates(mod);
		}
	}
	
	@Override
	public void registerListener(UpdateStateListener listener) {
		synchronized (stateListeners) {
			stateListeners.add(listener);
		}
	}

	@Override
	public void unregisterListener(UpdateStateListener listener) {
		synchronized (stateListeners) {
			stateListeners.remove(listener);
		}
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		synchronized (stateListeners) {
			for (UpdateStateListener listener : stateListeners) {
				listener.onStateChange(mod);
			}
		}
	}

	@Override
	public void onDownloadProgressChange(UpdatableMod mod) {
		synchronized (stateListeners) {
			for (UpdateStateListener listener : stateListeners) {
				listener.onDownloadProgress(mod);
			}
		}
	}

}
