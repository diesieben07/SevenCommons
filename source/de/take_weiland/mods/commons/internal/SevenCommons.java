package de.take_weiland.mods.commons.internal;

import java.util.Map;
import java.util.logging.Logger;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.internal.transformers.PacketTransformer;
import de.take_weiland.mods.commons.internal.transformers.mc.EntityAIMateTransformer;
import de.take_weiland.mods.commons.internal.transformers.mc.EntityPlayerTransformer;
import de.take_weiland.mods.commons.internal.transformers.mc.EntityZombieTransformer;
import de.take_weiland.mods.commons.internal.transformers.mc.GuiScreenTransformer;
import de.take_weiland.mods.commons.internal.transformers.mc.ItemBlockTransformer;
import de.take_weiland.mods.commons.internal.updater.UpdateInstaller;

@MCVersion("1.6.2")
@TransformerExclusions({
		"de.take_weiland.mods.commons.internal.",
		"de.take_weiland.mods.commons.util"		
	})
public class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.internal.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
	public static final Logger LOGGER = Logger.getLogger("SevenCommons");
	
	@Override
	@Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
			EntityPlayerTransformer.class.getCanonicalName(),
			EntityAIMateTransformer.class.getCanonicalName(),
			EntityZombieTransformer.class.getCanonicalName(),
			PacketTransformer.class.getCanonicalName(),
			ItemBlockTransformer.class.getCanonicalName(),
			GuiScreenTransformer.class.getCanonicalName()
		};
	}

	@Override
	public String getModContainerClass() {
		return CommonsModContainer.class.getCanonicalName();
	}

	@Override
	public String getSetupClass() {
		return UpdateInstaller.class.getCanonicalName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		MCP_ENVIRONMENT = !((Boolean)data.get("runtimeDeobfuscationEnabled")).booleanValue();
	}
}
