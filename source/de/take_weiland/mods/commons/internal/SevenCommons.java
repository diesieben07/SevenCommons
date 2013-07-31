package de.take_weiland.mods.commons.internal;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.asm.proxy.TargetClass;
import de.take_weiland.mods.commons.internal.proxy.NBTListProxy;

@MCVersion("1.6.2")
@TransformerExclusions({
		"de.take_weiland.mods.commons.internal.",
		"de.take_weiland.mods.commons.util."		
	})
public class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.internal.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER = Logger.getLogger("SevenCommons");
	public static final String MINECRAFT_VERSION = "1.6.2";
	
	public static File source;
	
	private static Multimap<String, Class<?>> proxyInterfaces = HashMultimap.create();
	
	static {
		
		registerProxyInterface(NBTListProxy.class);
		
	}
	
	public static void registerProxyInterface(Class<?> proxy) {
		if (!proxy.isInterface()) {
			throw new IllegalArgumentException("ProxyInterface " + proxy.getSimpleName() + " is not an interface!");
		}
		
		if (!proxy.isAnnotationPresent(TargetClass.class)) {
			throw new IllegalArgumentException("ProxyInterface " + proxy.getSimpleName() + " is missing @TargetClass annotation!");
		}
		
		String target = proxy.getAnnotation(TargetClass.class).value();
		proxyInterfaces.put(target, proxy);
	}
	
	public static Collection<Class<?>> getProxyInterfaces(String className) {
		return proxyInterfaces.get(className);
	}
	
	public static boolean hasProxyInterface(String className) {
		return proxyInterfaces.containsKey(className);
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			"de.take_weiland.mods.commons.internal.transformers.mc.EntityPlayerTransformer",
			"de.take_weiland.mods.commons.internal.transformers.mc.EntityAIMateTransformer",
			"de.take_weiland.mods.commons.internal.transformers.mc.EntityZombieTransformer",
			"de.take_weiland.mods.commons.internal.transformers.mc.ItemBlockTransformer",
			"de.take_weiland.mods.commons.internal.transformers.mc.GuiScreenTransformer",
			"de.take_weiland.mods.commons.internal.transformers.PacketTransformer",
			"de.take_weiland.mods.commons.internal.transformers.ProxyInterfaceInjector"
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
