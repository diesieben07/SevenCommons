package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.List;

import static de.take_weiland.mods.commons.internal.updater.ModVersion.MOD_VERSION_ORDERING;

public final class ModVersionCollection {

	private static final Predicate<ModVersion> USEABLE_FILTER = new Predicate<ModVersion>() {
		
		@Override
		public boolean apply(ModVersion version) {
			return version.isUseable();
		}
		
	};

	private static final Function<ModVersion, String> GET_VERSION_STRING = new Function<ModVersion, String>() {
		@Override
		public String apply(ModVersion input) {
			return input.getModVersion().getVersionString();
		}
	};
	
	private ModVersion currentVersionDummy;
	private ModVersion currentVersionActual;
	private int selectedVersion;

	private List<ModVersion> versions;

	public ModVersionCollection(ArtifactVersion currentVersion) {
		this.currentVersionDummy = new LocalModVersion(this, currentVersion, null, null, ImmutableList.<Dependency>of());
		versions = ImmutableList.of(currentVersionDummy);
		selectedVersion = 0;
	}

	public void injectAvailableVersions(List<ModVersion> unsortedVersions, ModVersion current) {
		if (current == null) {
			current = currentVersionActual = currentVersionDummy;
		} else {
			currentVersionActual = current;
		}
		versions = MOD_VERSION_ORDERING.immutableSortedCopy(Iterables.concat(unsortedVersions, ImmutableList.of(current)));
	}

	public ModVersion getSelectedVersion() {
		return JavaUtils.get(versions, selectedVersion);
	}

	public boolean selectVersion(int index) {
		if (versions != null && index >= 0 && index < versions.size()) {
			boolean change = selectedVersion != index;
			selectedVersion = index;
			return change;
		}
		return false;
	}

	public boolean isOptimalVersionSelected() {
		return selectedVersion == Iterables.indexOf(versions, USEABLE_FILTER);
	}

	public boolean selectOptimalVersion() {
		int optimal = Math.max(0, Iterables.indexOf(versions, USEABLE_FILTER));
		boolean change = optimal != selectedVersion;
		selectedVersion = optimal;
		return change;
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
