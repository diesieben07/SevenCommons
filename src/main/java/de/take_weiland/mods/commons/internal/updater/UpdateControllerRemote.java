package de.take_weiland.mods.commons.internal.updater;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import de.take_weiland.mods.commons.internal.PacketUpdaterAction;
import de.take_weiland.mods.commons.internal.PacketVersionSelect;
import de.take_weiland.mods.commons.net.DataBuf;

import java.util.Arrays;
import java.util.List;

import static de.take_weiland.mods.commons.internal.PacketUpdaterAction.Action.*;

/**
 * @author diesieben07
 */
public class UpdateControllerRemote extends AbstractUpdateController {

	private int percent;

	@Override
	public void searchForUpdates() {
		new PacketUpdaterAction(REFRESH).sendToServer();
	}

	@Override
	public void optimizeVersionSelection() {
		new PacketUpdaterAction(OPTIMIZE).sendToServer();
	}

	@Override
	public void performInstall() {
		new PacketUpdaterAction(INSTALL).sendToServer();
	}

	@Override
	public void restartMinecraft() {
		new PacketUpdaterAction(RESTART).sendToServer();
	}

	@Override
	public void resetFailure() {
		new PacketUpdaterAction(RESET).sendToServer();
	}

	@Override
	public int getDownloadPercent() {
		return percent;
	}

	public void setDownloadPercent(int percent) {
		this.percent = percent;
	}

	@Override
	public void onVersionSelect(UpdatableMod mod, int index) {
		new PacketVersionSelect(mod.getModId(), index).sendToServer();
	}

	public void readStateCounts(DataBuf buf) {
		for (int i = 0, len = stateCount.length; i < len; ++i) {
			stateCount[i] = buf.getVarInt();
		}
	}

	private static ArtifactVersion readArtifactVerion(DataBuf in) {
		String label = in.getString();
		String version = in.getString();
		return new DefaultArtifactVersion(label, version);
	}

	private List<Dependency> readDependencies(DataBuf in) {
		int len = in.getVarInt();
		List<Dependency> dependencies = Arrays.asList(new Dependency[len]);
		for (int i = 0; i < len; ++i) {
			dependencies.set(i, DependencyImpl.read(in));
		}
		return dependencies;
	}

	private void readVersions(DataBuf in, ModVersionCollection c) {
		int len = in.getVarInt();
		List<ModVersion> l = Arrays.asList(new ModVersion[len]);
		for (int i = 0; i < len; ++i) {
			l.set(i, LocalModVersion.read(in));
		}
		c.setVersionsForRemote(l);
		c.selectVersion(in.getVarInt());
	}

	public void readMods(DataBuf buf) {
		int len = buf.getVarInt();
		ImmutableMap.Builder<String, UpdatableMod> b = ImmutableMap.builder();
		for (int i = 0; i < len; ++i) {
			String id = buf.getString();
			String name = buf.getString();
			boolean internal = buf.getBoolean();
			ArtifactVersion installedVersion = readArtifactVerion(buf);
			RemoteUpdatableMod mod = new RemoteUpdatableMod(this, new ModVersionCollection(installedVersion), id, name, internal);

			readVersions(buf, mod.getVersions());

			b.put(id, mod);
		}
		mods = b.build();

		readStateCounts(buf);
	}

}
