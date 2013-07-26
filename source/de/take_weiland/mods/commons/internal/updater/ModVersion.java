package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import de.take_weiland.mods.commons.internal.SevenCommons;

public class ModVersion implements Comparable<ModVersion> {
	
	public final ArtifactVersion modVersion;
	public final String minecraftVersion;
	public final String downloadURL;
	public final String patchNotes;
	
	public ModVersion(ArtifactVersion modVersion, String minecraftVersion, String downloadURL, String patchNotes) {
		this.modVersion = modVersion;
		this.minecraftVersion = minecraftVersion;
		this.downloadURL = downloadURL;
		this.patchNotes = patchNotes;
	}
	
	public ModVersion(ArtifactVersion currentVersion) {
		this(currentVersion, SevenCommons.MINECRAFT_VERSION, null, null);
	}

	@Override
	public int compareTo(ModVersion o) {
		return modVersion.compareTo(o.modVersion);
	}
	
	public boolean canBeInstalled() {
		return minecraftVersion.equals(SevenCommons.MINECRAFT_VERSION);
	}
}
