package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.asm.ASMUtils;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.Type;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

@MCVersion(SevenCommons.MINECRAFT_VERSION)
@TransformerExclusions({
		"de.take_weiland.mods.commons.asm.",
		"de.take_weiland.mods.commons.network.",
		"de.take_weiland.mods.commons.internal.transformers",
		"de.take_weiland.mods.commons.fastreflect.dyn."
		})
public final class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.internal.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER;
	public static final String MINECRAFT_VERSION = "1.6.4";
	public static final String VERSION = "@VERSION@";	
	
	public static LaunchClassLoader CLASSLOADER;
	
	static File source;
	
	static {
		FMLLog.makeLog("SevenCommons");
		LOGGER = Logger.getLogger("SevenCommons");
	}
	
	public static final Type ENTITY_PLAYER = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.player.EntityPlayer"));
	public static final Type ENTITY_LIVING_BASE = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.EntityLivingBase"));
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			"de.take_weiland.mods.commons.internal.transformers.EntityAIMateTransformer",
			"de.take_weiland.mods.commons.internal.transformers.EntityPlayerTransformer",
			"de.take_weiland.mods.commons.internal.transformers.EntityZombieTransformer",
			"de.take_weiland.mods.commons.internal.transformers.GuiScreenTransformer",
			"de.take_weiland.mods.commons.internal.transformers.SyncingTransformer",
			"de.take_weiland.mods.commons.internal.transformers.EntityTrackerEntryTransformer",
			"de.take_weiland.mods.commons.sync.EntityTransformer",
			"de.take_weiland.mods.commons.internal.transformers.PacketTransformer",
			"de.take_weiland.mods.commons.internal.transformers.TraitAddingTransformer"
		};
	}

	@Override
	public String getModContainerClass() {
		return "de.take_weiland.mods.commons.internal.SCModContainer";
	}

	@Override
	public String getSetupClass() {
		return "de.take_weiland.mods.commons.internal.SCCallHook";
	}

	@Override
	public void injectData(Map<String, Object> data) {
		MCP_ENVIRONMENT = !((Boolean)data.get("runtimeDeobfuscationEnabled")).booleanValue();
		source = (File)data.get("coremodLocation");
		if (source == null) {
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
