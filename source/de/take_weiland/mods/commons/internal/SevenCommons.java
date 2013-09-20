package de.take_weiland.mods.commons.internal;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.Type;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asmproxy.ProxyInterfaceRegistry;
import de.take_weiland.mods.commons.util.EntityRendererProxy;
import de.take_weiland.mods.commons.util.EntityTrackerProxy;
import de.take_weiland.mods.commons.util.NBTListProxy;

@MCVersion(SevenCommons.MINECRAFT_VERSION)
@TransformerExclusions({
		"de.take_weiland.mods.commons.asm.",
		"de.take_weiland.mods.commons.util.",
		"de.take_weiland.mods.commons.network."
		})
public final class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.asm.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER;
	public static final String MINECRAFT_VERSION = "1.6.3";
	
	public static LaunchClassLoader CLASSLOADER;
	
	static File source;
	
	static {
		FMLLog.makeLog("SevenCommons");
		LOGGER = Logger.getLogger("SevenCommons");
		
		ProxyInterfaceRegistry.registerProxyInterface(NBTListProxy.class);
		ProxyInterfaceRegistry.registerProxyInterface(EntityRendererProxy.class);
		ProxyInterfaceRegistry.registerProxyInterface(EntityTrackerProxy.class);
		
	}
	
	public static final Type ENTITY_PLAYER = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.player.EntityPlayer"));
	public static final Type ENTITY_LIVING_BASE = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.EntityLivingBase"));
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			"de.take_weiland.mods.commons.asm.transformers.EntityAIMateTransformer",
			"de.take_weiland.mods.commons.asm.transformers.EntityPlayerTransformer",
			"de.take_weiland.mods.commons.asm.transformers.EntityZombieTransformer",
			"de.take_weiland.mods.commons.asm.transformers.GuiScreenTransformer",
			"de.take_weiland.mods.commons.asm.transformers.PacketTransformer",
			"de.take_weiland.mods.commons.asmproxy.ProxyInterfaceInjector",
			"de.take_weiland.mods.commons.asm.transformers.SyncedTransformer"
		};
	}

	@Override
	public String getModContainerClass() {
		return "de.take_weiland.mods.commons.internal.CommonsModContainer";
	}

	@Override
	public String getSetupClass() {
		return "de.take_weiland.mods.commons.internal.SevenCommonsCallHook";
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
	
	@Override
	@Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}
}
