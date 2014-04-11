package de.take_weiland.mods.commons.internal.updater;

import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.Restriction;
import cpw.mods.fml.common.versioning.VersionParser;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

import static net.minecraft.util.EnumChatFormatting.RESET;

/**
 * @author diesieben07
 */
public class Dependency {

	public final String modId;
	public final ArtifactVersion version;
	private final UpdateController controller;

	public Dependency(UpdateController controller, String modId, ArtifactVersion version) {
		this.controller = controller;
		this.modId = modId;
		this.version = version;
	}

	public boolean isSatisfied() {
		UpdatableMod mod = controller.getMod(modId);
		if (mod == null) {
			return false;
		}
		ModVersion targetVersion = mod.getVersions().getSelectedVersion();
		return targetVersion != null && version.containsVersion(targetVersion.getModVersion());
	}

	public void write(WritableDataBuf out) {
		out.putString(modId);
		out.putString(version.getLabel());
		out.putString(version.getRangeString());
		out.putString(getDisplay());
		out.putBoolean(isSatisfied());
	}

	public static Dependency read(UpdateController controller, DataBuf buf) {
		String modId = buf.getString();
		String label = buf.getString();
		String range = buf.getString();
		DefaultArtifactVersion version = new DefaultArtifactVersion(label, VersionParser.parseRange(range));
		return new Dependency(controller, modId, version);
	}

	@Override
	public String toString() {
		return "Dependency{" +
				"modId='" + modId + '\'' +
				", version=" + version.getRangeString() +
				'}';
	}

	private String displayCache;
	private boolean displayHasMod = false;
	private static final EnumChatFormatting BOUND_INCL = EnumChatFormatting.DARK_GREEN;
	private static final EnumChatFormatting BOUND_EXCL = EnumChatFormatting.RED;

	private static final String BOUND_INKL_LOWER = "(";
	private static final String BOUND_INCL_UPPER = ")";
	private static final String BOUND_EXCL_LOWER = "[";
	private static final String BOUND_EXCL_UPPER = "]";

	public String getDisplay() {
		if (!displayHasMod) {
			UpdatableMod mod = controller.getMod(modId);
			if (mod != null) {
				displayCache = getDisplay0(mod.getName());
				displayHasMod = true;
			} else if (displayCache == null) {
				displayCache = getDisplay0(modId);
			}
		}
		return displayCache;
	}

	private String getDisplay0(String modName) {
		StringBuilder s = new StringBuilder();

		s.append(modName);
		s.append(" at ");

		List<Restriction> restrictions = ((DefaultArtifactVersion) version).getRange().getRestrictions();
		int len = restrictions.size();
		for (int i = 0; i < len; ++i) {
			Restriction r = restrictions.get(i);
			String upper = r.getUpperBound().getVersionString();
			String lower = r.getLowerBound().getVersionString();
			if (upper.equals(lower)) {
				s.append(BOUND_INCL);
				s.append(upper);
				s.append(RESET);
			} else {
				boolean incl = r.isLowerBoundInclusive();
				s.append(incl ? BOUND_INKL_LOWER : BOUND_EXCL_LOWER);
				s.append(incl ? BOUND_INCL : BOUND_EXCL);
				s.append(lower);
				s.append(RESET);

				s.append('-');
				incl = r.isUpperBoundInclusive();
				s.append(incl ? BOUND_INCL : BOUND_EXCL);
				s.append(upper);
				s.append(RESET);
				s.append(incl ? BOUND_INCL_UPPER : BOUND_EXCL_UPPER);
			}

			if (i < len - 1) {
				s.append(" or ");
			}
		}
		return s.toString();
	}
}
