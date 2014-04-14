package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.Collection;

public abstract class AbstractUpdateController implements UpdateController {

	protected static final Function<UpdatableMod, String> ID_RETRIEVER = new Function<UpdatableMod, String>() {
		
		@Override
		public String apply(UpdatableMod mod) {
			return mod.getModId();
		}
		
	};

	final int[] stateCount = new int[JavaUtils.getEnumConstantsShared(ModUpdateState.class).length];
	ImmutableMap<String, ? extends UpdatableMod> mods = ImmutableMap.of();

	@Override
	public Collection<? extends UpdatableMod> getMods() {
		return mods.values();
	}

	@Override
	public UpdatableMod getMod(String modId) {
		return mods.get(modId);
	}

	@Override
	public boolean isSelectionValid() {
		boolean canInstall = true;
		boolean oneToInstall = false;
		for (UpdatableMod mod : mods.values()) {
			ModVersion selected = mod.getVersions().getSelectedVersion();
			if (!selected.isUseable()) {
				canInstall = false;
				break;
			}
			oneToInstall = oneToInstall || selected.canBeInstalled();
		}
		return oneToInstall && canInstall;
	}

	@Override
	public boolean isSelectionOptimized() {
		for (UpdatableMod mod : mods.values()) {
			if (!mod.getVersions().isOptimalVersionSelected()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isRefreshing() {
		return modsInState(ModUpdateState.REFRESHING) > 0;
	}

	@Override
	public void onStateChange(UpdatableMod mod, ModUpdateState oldState) {
		if (--stateCount[oldState.ordinal()] < 0) {
			stateCount[oldState.ordinal()] = 0;
		}
		++stateCount[mod.getState().ordinal()];
	}

	@Override
	public int modsInState(ModUpdateState state) {
		return stateCount[state.ordinal()];
	}

	@Override
	public boolean isInstalling() {
		return modsInState(ModUpdateState.INSTALLING) > 0;
	}

	@Override
	public boolean hasFailed() {
		return modsInState(ModUpdateState.INSTALL_FAIL) > 0;
	}

	@Override
	public boolean isRestartPending() {
		// we need to restart if no mod is still installing,
		// no mod has failed installing
		// and at least one has finished installing successfully
		return modsInState(ModUpdateState.INSTALLING) == 0
				&& modsInState(ModUpdateState.INSTALL_FAIL) == 0
				&& modsInState(ModUpdateState.INSTALL_OK) > 0;
	}
}
