package de.take_weiland.mods.commons.internal.updater;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.updater.ModVersionCollection.ModVersion;
import de.take_weiland.mods.commons.internal.updater.tasks.InstallUpdate;
import de.take_weiland.mods.commons.internal.updater.tasks.SearchUpdates;

public class UpdateControllerLocal implements UpdateController {

	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	public static final Logger LOGGER;
	
	static {
		FMLLog.makeLog(LOG_CHANNEL);
		LOGGER = Logger.getLogger(LOG_CHANNEL);
	}

	private Executor executor;

	private final Set<UpdateStateListener> listeners = Sets.newHashSet();
	private final Map<ModContainer, ModsFolderMod> mods;
	
	public UpdateControllerLocal() {
		mods = Maps.toMap(Loader.instance().getActiveModList(), new Function<ModContainer, ModsFolderMod>() {
			
			public ModsFolderMod apply(ModContainer mod) {
				return new ModsFolderMod(UpdateControllerLocal.this, mod);
			}
			
		});
		executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build());
	}
	
	@Override
	public UpdatableMod getMod(ModContainer modContainer) {
		UpdatableMod mod = mods.get(modContainer);
		if (mod == null) {
			throw new IllegalArgumentException(String.format("Mod %s, hasn't been registered to the UpdateController!", modContainer.getModId()));
		}
		return mod;
	}
	
	@Override
	public Collection<ModsFolderMod> getMods() {
		return mods.values();
	}
	
	@Override
	public void searchForUpdates() {
		for (UpdatableMod mod : mods.values()) {
			searchForUpdates(mod);
		}
	}
	
	@Override
	public void searchForUpdates(UpdatableMod mod) {
		validate(mod);
		if (mod.transition(ModUpdateState.CHECKING)) {
			executor.execute(new SearchUpdates(mod));
		}
	}
	
	@Override
	public void update(UpdatableMod mod, ModVersion version) {
		validate(mod);
		if (mod.transition(ModUpdateState.DOWNLOADING)) {
			executor.execute(new InstallUpdate(mod, version));
		}
	}
	
	private void validate(UpdatableMod mod) {
		if (!mods.containsKey(mod.getContainer())) { // check for the container here since key searching is faster
			throw new IllegalArgumentException(String.format("Mod %s not valid for this UpdateController!", mod.getContainer().getModId()));
		}
	}
	
	@Override
	public void registerListener(UpdateStateListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void unregisterListener(UpdateStateListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		synchronized (listeners) {
			for (UpdateStateListener listener : listeners) {
				listener.onStateChange(mod);
			}
		}
	}
	
	@Override
	public void onUpdateProgress(UpdatableMod mod, int progress, int total) {
		synchronized (listeners) {
			for (UpdateStateListener listener : listeners) {
				listener.onDownloadProgress(mod, progress, total);
			}
		}
	}
}
