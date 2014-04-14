package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.Collections;
import java.util.List;

import static de.take_weiland.mods.commons.internal.updater.ModVersion.MOD_VERSION_ORDERING;

public final class ModVersionCollection {

	private static final Predicate<ModVersion> USEABLE_FILTER = new Predicate<ModVersion>() {
		
		@Override
		public boolean apply(ModVersion version) {
			return version.isUseable();
		}
		
	};

	private UpdatableMod mod;
	private ModVersion currentVersionDummy;
	private ModVersion currentVersionActual;
	private int selectedVersion;

	private List<ModVersion> versions;

	public ModVersionCollection(ArtifactVersion currentVersion) {
		currentVersionActual = this.currentVersionDummy = new LocalModVersion(this, currentVersion, null, null, ImmutableList.<Dependency>of());
		versions = ImmutableList.of(currentVersionDummy);
		selectedVersion = 0;
	}

	public void setMod(UpdatableMod mod) {
		this.mod = mod;
	}

	public void injectAvailableVersions(List<ModVersion> unsortedVersions, ModVersion current) {
		if (current == null) {
			current = currentVersionActual = currentVersionDummy;
		} else {
			currentVersionActual = current;
		}
		versions = MOD_VERSION_ORDERING.immutableSortedCopy(Iterables.concat(unsortedVersions, Collections.singleton(current)));
	}

	public void setVersionsForRemote(List<ModVersion> versions) {
		this.versions = ImmutableList.copyOf(Iterables.concat(versions, Collections.singleton(currentVersionActual)));
	}

	public ModVersion getSelectedVersion() {
		return JavaUtils.get(versions, selectedVersion);
	}

	public int getSelectedVersionIndex() {
		return selectedVersion;
	}

	public boolean selectVersion(int index) {
		if (versions != null && index >= 0 && index < versions.size()) {
			boolean change = selectedVersion != index;
			selectedVersion = index;
			if (change) {
				mod.getController().onVersionSelect(mod, index);
			}
			return change;
		}
		return false;
	}

	public boolean isOptimalVersionSelected() {
		return selectedVersion == Iterables.indexOf(versions, USEABLE_FILTER);
	}

	public boolean selectOptimalVersion() {
		int optimal = Math.max(0, Iterables.indexOf(versions, USEABLE_FILTER));
		return selectVersion(optimal);
	}

	public ModVersion getCurrentVersion() {
		return currentVersionActual;
	}
	
	public List<ModVersion> getAvailableVersions() {
		return versions;
	}
	
	public boolean isInstallable(ModVersion version) {
		return version.canBeInstalled() && versions.contains(version);
	}

}
