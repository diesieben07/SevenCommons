package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.versioning.ArtifactVersion;

import java.util.List;

public final class LocalModVersion implements ModVersion {

	protected final ModVersionCollection versionCollection;
	protected final ArtifactVersion modVersion;
	protected final String downloadURL;
	protected final String patchNotes;
	protected final List<Dependency> dependencies;

	public LocalModVersion(ModVersionCollection collection, ArtifactVersion modVersion, String downloadURL, String patchNotes, List<Dependency> dependencies) {
		this.modVersion = modVersion;
		this.versionCollection = collection;
		this.dependencies = dependencies;
		this.downloadURL = downloadURL;
		this.patchNotes = patchNotes;
	}

	private boolean checkDependencies() {
		for (Dependency dep : dependencies) {
			if (!dep.isSatisfied()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isUseable() {
		return isInstalled() || canBeInstalled();
	}

	@Override
	public boolean isInstalled() {
		ModVersion current = versionCollection.getCurrentVersion();
		return current == this || getModVersion().getVersionString().equals(current.getModVersion().getVersionString());
	}

	@Override
	public boolean canBeInstalled() {
		return !isInstalled() && checkDependencies();
	}

	@Override
	public ArtifactVersion getModVersion() {
		return modVersion;
	}

	@Override
	public String getDownloadURL() {
		return downloadURL;
	}

	@Override
	public String getPatchNotes() {
		return patchNotes;
	}

	@Override
	public List<Dependency> getDependencies() {
		return dependencies;
	}
}
