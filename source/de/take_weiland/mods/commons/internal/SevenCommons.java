package de.take_weiland.mods.commons.internal;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import de.take_weiland.mods.commons.internal.transformers.EntityAIMateTransformer;
import de.take_weiland.mods.commons.internal.transformers.EntityPlayerTransformer;
import de.take_weiland.mods.commons.internal.transformers.EntityZombieTransformer;

@MCVersion("1.6.2")
@TransformerExclusions("de.take_weiland.mods.commons.internal.")
public class SevenCommons implements IFMLLoadingPlugin {

	public static final String ASM_HOOK_CLASS = "de.take_weiland.mods.commons.internal.ASMHooks";
	public static boolean MCP_ENVIRONMENT;
	
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
			EntityZombieTransformer.class.getCanonicalName()
		};
	}

	@Override
	public String getModContainerClass() {
		return CommonsModContainer.class.getCanonicalName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		MCP_ENVIRONMENT = !((Boolean)data.get("runtimeDeobfuscationEnabled")).booleanValue();
	}
}
