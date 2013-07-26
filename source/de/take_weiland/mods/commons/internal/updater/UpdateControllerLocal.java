package de.take_weiland.mods.commons.internal.updater;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import de.take_weiland.mods.commons.internal.updater.tasks.InstallUpdate;
import de.take_weiland.mods.commons.internal.updater.tasks.SearchUpdates;

public class UpdateControllerLocal implements UpdateController {

	static final List<String> INTERNAL_MODS = Arrays.asList("mcp", "forge", "fml", "minecraft");
	private static final String LOG_CHANNEL = "Sevens ModUpdater";
	public static final Logger LOGGER;
	
	static {
		FMLLog.makeLog(LOG_CHANNEL);
		LOGGER = Logger.getLogger(LOG_CHANNEL);
	}

	private Executor executor;

	private final Set<UpdateStateListener> listeners = Sets.newHashSet();
	private final List<UpdatableMod> mods;
	
	public UpdateControllerLocal() {
		mods = ImmutableList.copyOf(Lists.transform(Loader.instance().getActiveModList(), new Function<ModContainer, UpdatableMod>() {
			
			public UpdatableMod apply(ModContainer mod) {
				if (INTERNAL_MODS.contains(mod.getModId().toLowerCase())) {
					return new FMLInternalMod(mod, UpdateControllerLocal.this);
				} else {
					return new ModsFolderMod(mod, UpdateControllerLocal.this);
				}
			}
			
		}));
		executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("Sevens ModUpdater %d").build());
	}
	
	@Override
	public List<UpdatableMod> getMods() {
		return mods;
	}
	
	@Override
	public void searchForUpdates() {
		for (UpdatableMod mod : mods) {
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
		if (!mods.contains(mod)) {
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
	
}
