package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.net.WritableDataBuf;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author diesieben07
 */
public class RemoteModVersion implements ModVersion {

	private final boolean isInstalled;
	private final boolean canBeInstalled;
	private final String version;
	private final String patchNotes;
	private final List<String> dependencies;

	RemoteModVersion(boolean isInstalled, boolean canBeInstalled, String version, String patchNotes, List<String> dependencies) {
		this.isInstalled = isInstalled;
		this.canBeInstalled = canBeInstalled;
		this.version = version;
		this.patchNotes = patchNotes;
		this.dependencies = dependencies;
	}

	@Override
	public boolean isUseable() {
		return isInstalled() || canBeInstalled();
	}

	@Override
	public boolean isInstalled() {
		return isInstalled;
	}

	@Override
	public boolean canBeInstalled() {
		return canBeInstalled;
	}

	@Override
	public ArtifactVersion getModVersion() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getVersionString() {
		return version;
	}

	@Override
	public String getDownloadURL() {
		return "";
	}

	@Override
	public String getPatchNotes() {
		return patchNotes;
	}

	@Override
	public List<Dependency> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getDependencyDisplay() {
		return dependencies;
	}

	@Override
	public void write(WritableDataBuf buf) {
		throw new UnsupportedOperationException();
	}
}
