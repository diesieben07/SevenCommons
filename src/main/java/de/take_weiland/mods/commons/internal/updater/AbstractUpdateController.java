package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;

public abstract class AbstractUpdateController implements UpdateController {

	protected static final Function<UpdatableMod, String> ID_RETRIEVER = new Function<UpdatableMod, String>() {
		
		@Override
		public String apply(UpdatableMod mod) {
			return mod.getModId();
		}
		
	};
	
	protected ImmutableMap<String, ? extends UpdatableMod> mods = ImmutableMap.of();

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
	public void onStateChange(UpdatableMod mod, ModUpdateState oldState) { }
}
