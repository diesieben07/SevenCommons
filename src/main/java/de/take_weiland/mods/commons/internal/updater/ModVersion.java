package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;

public final class ModVersion {
	
	public final UpdatableMod mod;
	public final ArtifactVersion modVersion;
	public final String minecraftVersion;
	public final String downloadURL;
	public final String patchNotes;
	
	public ModVersion(UpdatableMod mod, ArtifactVersion modVersion, String minecraftVersion, String downloadURL, String patchNotes) {
		this.mod = mod;
		this.modVersion = modVersion;
		this.minecraftVersion = minecraftVersion;
		this.downloadURL = downloadURL;
		this.patchNotes = patchNotes;
	}
	
	public ModVersion(UpdatableMod mod, ArtifactVersion currentVersion) {
		this(mod, currentVersion, SevenCommons.MINECRAFT_VERSION, null, null);
	}

	public boolean canBeInstalled() {
		return modVersion.compareTo(mod.getVersions().getCurrentVersion().modVersion) != 0 && minecraftVersion.equals(SevenCommons.MINECRAFT_VERSION);
	}
	
	public void write(WritableDataBuf out) {
		out.putString(modVersion.getVersionString());
		out.putString(Strings.nullToEmpty(minecraftVersion));
		out.putString(Strings.nullToEmpty(patchNotes));
	}
	
	public static ModVersion read(UpdatableMod mod, DataBuf in) {
		ArtifactVersion version = new DefaultArtifactVersion(in.getString());
		String minecraftVersion = in.getString();
		String patchNotes = in.getString();
		return new ModVersion(mod, version, minecraftVersion, null, patchNotes);
	}

	public static final Ordering<ModVersion> MOD_VERSION_ORDERING = Ordering.from(new Comparator<ModVersion>() {

		@Override
		public int compare(ModVersion version1, ModVersion version2) {
			return version1.modVersion.compareTo(version2.modVersion);
		}
		
	}).reverse();
}
