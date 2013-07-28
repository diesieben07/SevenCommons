package de.take_weiland.mods.commons.internal.updater;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.util.CommonUtils;

public final class ModVersionCollection {

	private static final Predicate<ModVersion> INSTALLABLE_FILTER = new Predicate<ModVersion>() {
		
		@Override
		public boolean apply(ModVersion version) {
			return version.canBeInstalled();
		}
		
	};
	
	private int selectedVersion;
	
	private final ModVersion currentVersion;
	
	private List<ModVersion> versions;
	
	private List<ModVersion> installableVersions;
	
	public ModVersionCollection(UpdatableMod mod, ModVersion currentVersion) {
		this.currentVersion = currentVersion;
		installableVersions = versions = Collections.emptyList();
	}
	
	public ModVersionCollection(UpdatableMod mod, ArtifactVersion currentVersion) {
		this.currentVersion = new ModVersion(mod, currentVersion);
		installableVersions = versions = Collections.emptyList();
	}
	
	public void injectAvailableVersions(List<ModVersion> unsortedVersions) {
		versions = ModVersion.MOD_VERSION_ORDERING.immutableSortedCopy(unsortedVersions);
		installableVersions = ImmutableList.copyOf(Iterables.filter(versions, INSTALLABLE_FILTER));
		if (!installableVersions.isEmpty()) {
			selectedVersion = versions.indexOf(installableVersions.get(0));
		}
	}
	
	public ModVersion getSelectedVersion() {
		return CommonUtils.safeListAccess(versions, selectedVersion);
	}
	
	public void selectNextVersion() {
		if (versions != null) {
			selectedVersion++;
			if (selectedVersion >= versions.size()) {
				selectedVersion = 0;
			}
		}
	}
	
	public ModVersion getCurrentVersion() {
		return currentVersion;
	}
	
	public List<ModVersion> getAvailableVersions() {
		return versions;
	}
	
	public List<ModVersion> getInstallableVersions() {
		return installableVersions;
	}
	
	public ModVersion getNewestInstallableVersion() {
		return CommonUtils.safeListAccess(installableVersions, 0);
	}
	
	public ModVersion getNewestVersion() {
		return CommonUtils.safeListAccess(versions, 0);
	}
}
