package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import de.take_weiland.mods.commons.SevenCommonsWrapper;
import de.take_weiland.mods.commons.fastreflect.Fastreflect;
import de.take_weiland.mods.commons.util.MiscUtil;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

@MCVersion(SevenCommons.MINECRAFT_VERSION)
@IFMLLoadingPlugin.SortingIndex(1001) // get after deobfuscation
public final class SevenCommons implements IFMLLoadingPlugin {

	public static final String MINECRAFT_VERSION = "1.6.4";
	public static final String VERSION = "1.0";

	public static final InternalReflector REFLECTOR = Fastreflect.createAccessor(InternalReflector.class);
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER;
	public static File MINECRAFT_DIR;

	public static LaunchClassLoader CLASSLOADER = Launch.classLoader;

	public static File source;

	public static SCMetaInternalProxy metaProxy;
	
	static {
		LOGGER = MiscUtil.getLogger("SevenCommons");
	}

	public SevenCommons() {
		Properties props = System.getProperties();
		if (props.put(SevenCommonsWrapper.SYS_PROP_INSTANCE, this) != null) {
			throw new IllegalStateException("More than one instance of SevenCommons!");
		}
		props.put(SevenCommonsWrapper.SYS_PROP_VERSION, VERSION);

		String[] excl = {
				"de.take_weiland.mods.commons.asm.",
				"de.take_weiland.mods.commons.internal.transformers.",
				"de.take_weiland.mods.commons.internal.exclude.",
				"de.take_weiland.mods.commons.util.JavaUtils",
				"de.take_weiland.mods.commons.sync."
		};

		for (String e : excl) {
			Launch.classLoader.addTransformerExclusion(e);
		}
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			"de.take_weiland.mods.commons.internal.transformers.SCTransformerWrapper",
		};
	}

	@Override
	public String getModContainerClass() {
		return "de.take_weiland.mods.commons.internal.exclude.SCModContainer";
	}

	@Override
	public String getSetupClass() {
		return "de.take_weiland.mods.commons.internal.exclude.SCCallHook";
	}

	@Override
	public void injectData(Map<String, Object> data) {
		MCP_ENVIRONMENT = !((Boolean)data.get("runtimeDeobfuscationEnabled")).booleanValue();
		MINECRAFT_DIR = (File) data.get("mcLocation");
		source = (File)data.get("coremodLocation");
		if (source == null) { // this is usually in a dev env
			try {
				source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			} catch (URISyntaxException e) {
				LOGGER.severe("Failed to acquire source location for SevenCommons!");
				throw Throwables.propagate(e);
			}
		}
	}

	public static Logger scLogger() {
		return scLogger(Fastreflect.getCallerClass().getSimpleName());
	}

	public static Logger scLogger(String channel) {
		return MiscUtil.getLogger("SC|" + channel);
	}
}
