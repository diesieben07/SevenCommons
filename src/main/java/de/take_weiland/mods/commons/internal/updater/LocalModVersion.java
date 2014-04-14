package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.Sides;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class LocalModVersion implements ModVersion {

	protected final ModVersionCollection versionCollection;
	protected final ArtifactVersion modVersion;
	protected final String downloadURL;
	protected final String patchNotes;
	protected final List<Dependency> dependencies;
	private final Collection<String> depDisplay;

	public LocalModVersion(ModVersionCollection collection, ArtifactVersion modVersion, String downloadURL, String patchNotes, List<Dependency> dependencies) {
		this.modVersion = modVersion;
		this.versionCollection = collection;
		this.dependencies = dependencies;
		this.downloadURL = downloadURL;
		this.patchNotes = patchNotes;

		if (Sides.environment().isClient()) {
			depDisplay = Collections2.transform(dependencies, Functions.toStringFunction());
		} else {
			depDisplay = Collections.emptyList();
		}
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

	@Override
	public Collection<String> getDependencyDisplay() {
		return depDisplay;
	}

	@Override
	public String getVersionString() {
		return modVersion.getVersionString();
	}

	@Override
	public void write(WritableDataBuf buf) {
		buf.putByte((isInstalled() ? 1 : 0) | (canBeInstalled() ? 2 : 0));
		buf.putString(modVersion.getVersionString());
		buf.putString(Strings.nullToEmpty(patchNotes));
		buf.putVarInt(dependencies.size());
		for (Dependency d : dependencies) {
			buf.putString(d.getDisplay());
		}
	}


	public static RemoteModVersion read(DataBuf in) {
		int flags = in.getByte();
		String version = in.getString();
		String patchNotes = in.getString();
		int len = in.getVarInt();
		String[] deps = new String[len];
		for (int i = 0; i < len; ++i) {
			deps[i] = in.getString();
		}
		return new RemoteModVersion((flags & 1) != 0, (flags & 2) != 0, version, patchNotes, Arrays.asList(deps));
	}
}
