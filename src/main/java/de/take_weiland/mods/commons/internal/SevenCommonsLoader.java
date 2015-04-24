package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import de.take_weiland.mods.commons.util.Logging;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Logger;

@MCVersion("1.6.4")
@IFMLLoadingPlugin.SortingIndex(1001) // get after deobfuscation
@IFMLLoadingPlugin.TransformerExclusions({
		"de.take_weiland.mods.commons.asm.",
		"de.take_weiland.mods.commons.internal.transformers.",
		"de.take_weiland.mods.commons.internal.exclude.",
		"de.take_weiland.mods.commons.util.JavaUtils",
        "de.take_weiland.mods.commons.util.Logging",
		"de.take_weiland.mods.commons.reflect.",
		"de.take_weiland.mods.commons.sync.",
		"de.take_weiland.mods.commons.nbt.ToNbt"
})
public final class SevenCommonsLoader implements IFMLLoadingPlugin {

	public static File source;

	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
                "de.take_weiland.mods.commons.internal.transformers.SCVisitorTransformerWrapper"
		};
	}

	@Override
	public String getModContainerClass() {
		return "de.take_weiland.mods.commons.internal.SevenCommons";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		source = (File) data.get("coremodLocation");
		if (source == null) { // this is usually in a dev env
			try {
				source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			} catch (URISyntaxException e) {
				throw new RuntimeException("Failed to acquire source location for SevenCommons!", e);
			}
		}
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static Logger scLogger(String channel) {
		return Logging.getLogger("SC|" + channel);
	}
}
