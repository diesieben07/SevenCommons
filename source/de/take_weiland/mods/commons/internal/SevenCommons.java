package de.take_weiland.mods.commons.internal;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.asm.Type;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.proxy.ProxyInterfaceRegistry;
import de.take_weiland.mods.commons.util.NBTListProxy;

@MCVersion("1.6.2")
@TransformerExclusions({
		"de.take_weiland.mods.commons.asm.",
		"de.take_weiland.mods.commons.util.",
		"de.take_weiland.mods.commons.network.",
	})
public class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.asm.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER = Logger.getLogger("SevenCommons");
	public static final String MINECRAFT_VERSION = "1.6.2";
	
	static File source;
	
	static {
		
		ProxyInterfaceRegistry.registerProxyInterface(NBTListProxy.class);
		
	}
	
	public static final Type ENTITY_PLAYER = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.player.EntityPlayer"));
	public static final Type ENTITY_LIVING_BASE = Type.getObjectType(ASMUtils.makeNameInternal("net.minecraft.entity.EntityLivingBase"));
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			"de.take_weiland.mods.commons.asm.transformers.EntityAIMateTransformer",
			"de.take_weiland.mods.commons.asm.transformers.EntityPlayerMPTransformer",
			"de.take_weiland.mods.commons.asm.transformers.EntityPlayerTransformer",
			"de.take_weiland.mods.commons.asm.transformers.EntityZombieTransformer",
			"de.take_weiland.mods.commons.asm.transformers.GuiScreenTransformer",
			"de.take_weiland.mods.commons.asm.transformers.NetServerHandlerTransformer",
			"de.take_weiland.mods.commons.asm.transformers.PacketTransformer",
			"de.take_weiland.mods.commons.asm.proxy.ProxyInterfaceInjector"
		};
	}

	@Override
	public String getModContainerClass() {
		return "de.take_weiland.mods.commons.internal.CommonsModContainer";
	}

	@Override
	public String getSetupClass() {
		return "de.take_weiland.mods.commons.internal.updater.UpdateInstaller";
	}

	@Override
	public void injectData(Map<String, Object> data) {
		MCP_ENVIRONMENT = !((Boolean)data.get("runtimeDeobfuscationEnabled")).booleanValue();
		source = (File)data.get("coremodLocation");
		if (source == null) {
			try {
				source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				// oops
			}
		}
	}
	
	@Override
	@Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}
}
