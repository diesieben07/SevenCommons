package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.fastreflect.Fastreflect;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

@MCVersion(SevenCommons.MINECRAFT_VERSION)
@IFMLLoadingPlugin.SortingIndex(1001) // get after deobfuscation
@TransformerExclusions({
		"de.take_weiland.mods.commons.asm.",
		"de.take_weiland.mods.commons.network.",
		"de.take_weiland.mods.commons.net.",
		"de.take_weiland.mods.commons.internal.transformers.",
		"de.take_weiland.mods.commons.internal.exclude.",
		"de.take_weiland.mods.commons.subtypes.",
		"de.take_weiland.mods.commons.util.JavaUtils",
		"de.take_weiland.mods.commons.trait.",
		"de.take_weiland.mods.commons.sync."
		})
public final class SevenCommons implements IFMLLoadingPlugin {

	public static final InternalReflector REFLECTOR = Fastreflect.createAccessor(InternalReflector.class);
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER;
	public static final String MINECRAFT_VERSION = "1.6.4";
	public static final String VERSION = "@VERSION@";	
	
	public static LaunchClassLoader CLASSLOADER = (LaunchClassLoader) SevenCommons.class.getClassLoader();

	public static File source;
	
	static {
		FMLLog.makeLog("SevenCommons");
		LOGGER = Logger.getLogger("SevenCommons");
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
		source = (File)data.get("coremodLocation");
		if (source == null) { // this is usually in a dev env
			try {
				source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			} catch (URISyntaxException e) {
				LOGGER.severe("Failed to acquire source location for SevenCommons!");
				Throwables.propagate(e);
			}
		}
	}
	
//	@Override
//	@Deprecated
//	public String[] getLibraryRequestClass() {
//		return null;
//	}
}
