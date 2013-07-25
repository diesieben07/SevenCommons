package de.take_weiland.mods.commons.internal.updater;

import java.io.Reader;
import java.util.List;

import net.minecraft.crash.CallableMinecraftVersion;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import de.take_weiland.mods.commons.util.CommonUtils;

public final class ModVersionInfo {

	private static final JdomParser JSON_PARSER = new JdomParser();
	static final String MINECRAFT_VERSION = new CallableMinecraftVersion(null).minecraftVersion();
	private static final Predicate<ModVersion> INSTALLABLE_FILTER = new Predicate<ModVersion>() {
		
		@Override
		public boolean apply(ModVersion version) {
			return version.canBeInstalled();
		}
		
	};
	
	private static final String[] KEYS = new String[] {
		"version",	"minecraftVersion", "url"		
	};
	
	private int selectedVersion;
	
	private final ModVersion currentVersion;
	
	private final List<ModVersion> versions;
	
	private final List<ModVersion> installableVersions;
	
	private ModVersionInfo(List<ModVersion> versions, ModVersion currentVersion) {
		this.versions = versions;

		this.installableVersions = ImmutableList.copyOf(Iterables.filter(versions, INSTALLABLE_FILTER));
		
		this.currentVersion = currentVersion;
		
		this.selectedVersion = versions.indexOf(installableVersions.get(0));
	}
	
	public ModVersion getSelectedVersion() {
		return versions != null ? CommonUtils.safeListAccess(versions, selectedVersion) : null;
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
		return installableVersions.isEmpty() ? null : installableVersions.get(0);
	}
	
	public ModVersion getNewestVersion() {
		return versions.isEmpty() ? null : versions.get(0);
	}
	
	public static final ModVersionInfo create(Reader reader, ModContainer mod) throws InvalidModVersionException {
		try {
			ImmutableList.Builder<ModVersion> versions = ImmutableList.builder();
			JsonRootNode json = JSON_PARSER.parse(reader);
			
			if (!json.isArrayNode()) {
				invalid();
			}
			
			for (JsonNode versionNode : json.getElements()) {
				for (String key : KEYS) {
					if (!versionNode.isStringValue(key)) {
						invalid();
					}
				}
				
				String modVersion = versionNode.getStringValue("version");
				String minecraftVersion = versionNode.getStringValue("minecraftVersion");
				String url = versionNode.getStringValue("url");
				String patchNotes = versionNode.isStringValue("patchNotes") ? versionNode.getStringValue("patchNotes") : null;
				versions.add(new ModVersion(new DefaultArtifactVersion(modVersion), minecraftVersion, url, patchNotes));
			}
			return new ModVersionInfo(Ordering.natural().immutableSortedCopy(versions.build()), new ModVersion(mod.getProcessedVersion()));
		} catch (Throwable t) {
			t.printStackTrace();
			Throwables.propagateIfPossible(t);
			return invalid(t);
		}
	}
	
	private static final void invalid() throws InvalidModVersionException {
		throw new InvalidModVersionException("Failed to parse ModVersionInfo");
	}
	
	private static final ModVersionInfo invalid(Throwable t) throws InvalidModVersionException {
		throw new InvalidModVersionException("Failed to parse ModVersionInfo", t);
	}
	
	public static class ModVersion implements Comparable<ModVersion> {
		
		public final ArtifactVersion modVersion;
		public final String minecraftVersion;
		public final String downloadURL;
		public final String patchNotes;
		
		ModVersion(ArtifactVersion modVersion, String minecraftVersion, String downloadURL, String patchNotes) {
			this.modVersion = modVersion;
			this.minecraftVersion = minecraftVersion;
			this.downloadURL = downloadURL;
			this.patchNotes = patchNotes;
		}
		
		ModVersion(ArtifactVersion modVersion) {
			this(modVersion, MINECRAFT_VERSION, null, null);
		}

		@Override
		public int compareTo(ModVersion o) {
			return modVersion.compareTo(o.modVersion);
		}
		
		public boolean canBeInstalled() {
			return minecraftVersion.equals(MINECRAFT_VERSION);
		}
	}
}
