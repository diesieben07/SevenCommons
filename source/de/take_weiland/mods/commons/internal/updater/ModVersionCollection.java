package de.take_weiland.mods.commons.internal.updater;

import java.util.Collections;
import java.util.List;


import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import cpw.mods.fml.common.ModContainer;
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
	
	public ModVersionCollection(ModContainer mod) {
		currentVersion = new ModVersion(mod.getProcessedVersion());
		installableVersions = versions = Collections.emptyList();
	}
	
	public void injectAvailableVersions(List<ModVersion> unsortedVersions) {
		versions = Ordering.natural().reverse().immutableSortedCopy(unsortedVersions);
		installableVersions = ImmutableList.copyOf(Iterables.filter(versions, INSTALLABLE_FILTER));
		selectedVersion = versions.indexOf(installableVersions.get(0));
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
