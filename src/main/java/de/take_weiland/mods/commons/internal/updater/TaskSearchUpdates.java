package de.take_weiland.mods.commons.internal.updater;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;
import de.take_weiland.mods.commons.util.Scheduler;
import de.take_weiland.mods.commons.util.Sides;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

public class TaskSearchUpdates implements Runnable {

	private UpdatableMod mod;
	ModVersion currentVersionFound;
	
	public TaskSearchUpdates(UpdatableMod mod) {
		this.mod = mod;
	}

	@Override
	public void run() {
		URL url = mod.getUpdateURL();
		Reader reader = null;
		List<ModVersion> parsedVersions = null;
		try {
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			parsedVersions = parseVersionFile(reader);
		} catch (IOException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("IOException during update checking for mod %s", mod.getModId()));
			e.printStackTrace();
		} catch (InvalidModVersionException e) {
			UpdateControllerLocal.LOGGER.warning(String.format("Version Info-File for mod %s is invalid", mod.getModId()));
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
			final List<ModVersion> parsedVersionsFinal = parsedVersions;
			Scheduler.get(Sides.environment()).execute(new Runnable() {
				@Override
				public void run() {
					if (parsedVersionsFinal != null) {
						mod.getVersions().injectAvailableVersions(parsedVersionsFinal, currentVersionFound);
						mod.getController().optimizeVersionSelection();
					}
					mod.transition(ModUpdateState.AVAILABLE);
				}
			});
		}
	}
	
	private static final JdomParser JSON_PARSER = new JdomParser();

	private List<ModVersion> parseVersionFile(Reader reader) throws InvalidModVersionException {
		try {
			ImmutableList.Builder<ModVersion> versions = ImmutableList.builder();
			JsonRootNode json = JSON_PARSER.parse(reader);
			
			if (!json.isArrayNode()) {
				throw invalid();
			}

			String currentVersion = mod.getVersions().getCurrentVersion().getModVersion().getVersionString();
			for (JsonNode versionNode : json.getElements()) {
				List<Dependency> dependencies;
				if (versionNode.isStringValue("dependencies")) {
					dependencies = parseDependencies(mod.getController(), mod.getModId(), versionNode.getStringValue("dependencies"));
				} else {
					dependencies = ImmutableList.of();
				}

				String versionString = versionNode.getStringValue("version");
				String url = versionNode.getStringValue("url");
				String patchNotes = versionNode.isStringValue("patchNotes") ? versionNode.getStringValue("patchNotes") : null;

				ArtifactVersion artifactVersion = new DefaultArtifactVersion(mod.getModId(), versionString);
				ModVersion modVersion = new LocalModVersion(mod.getVersions(), artifactVersion, url, patchNotes, dependencies);
				if (versionString.equals(currentVersion)) {
					this.currentVersionFound = modVersion;
				} else {
					versions.add(modVersion);
				}
			}
			return versions.build();
		} catch (Throwable t) {
			t.printStackTrace();
			Throwables.propagateIfPossible(t);
			throw invalid(t);
		}
	}

	private static final Splitter DEPS_SPLITTER = Splitter.on(';');

	private static List<Dependency> parseDependencies(UpdateController controller, String modId, String deps) throws InvalidModVersionException {
		ImmutableList.Builder<Dependency> b = ImmutableList.builder();

		String failure = null;
		for (String dep : DEPS_SPLITTER.split(deps)) {
			dep = dep.trim();
			int atIndex = dep.indexOf('@');
			if (atIndex < 0) {
				failure = dep;
				break;
			}
			String mod = dep.substring(0, atIndex).trim();
			String versionSpec = dep.substring(atIndex + 1).trim();
			ArtifactVersion versionRange = new DefaultArtifactVersion(mod, VersionParser.parseRange(versionSpec));
			b.add(new Dependency(controller, mod, versionRange));
		}

		if (failure != null) {
			throw new InvalidModVersionException(String.format("Failed to parse dependencies for %s (\"%s\"", modId, deps));
		}

		return b.build();
	}
	
	private static InvalidModVersionException invalid() {
		return new InvalidModVersionException("Failed to parse ModVersionInfo");
	}
	
	private static InvalidModVersionException invalid(Throwable t) {
		return new InvalidModVersionException("Failed to parse ModVersionInfo", t);
	}
}