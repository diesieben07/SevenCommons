
package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.JavaCompatibility;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.11.2")
@IFMLLoadingPlugin.SortingIndex(1001) // get after deobfuscation
@IFMLLoadingPlugin.TransformerExclusions({
        "de.take_weiland.mods.commons.asm.",
        "de.take_weiland.mods.commons.internal.transformers.",
        "de.take_weiland.mods.commons.internal.exclude.",
        "de.take_weiland.mods.commons.util.JavaUtils",
        "de.take_weiland.mods.commons.util.Logging",
        "de.take_weiland.mods.commons.util.Listenable",
        "de.take_weiland.mods.commons.reflect.",
        "de.take_weiland.mods.commons.sync.",
        "de.take_weiland.mods.commons.nbt.ToNbt"
})
// warning! This class is compiled separately from all other SevenCommons classes
// this means: this class cannot reference anything else and nobody else can reference this
// this is because this class is compiled for Java 6 to enable the warning message
public final class SevenCommonsLoader implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        // must do this here, because constructor is too early for FMLCommonHandler
        JavaCompatibility.requireJava8(true);

        return new String[]{
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
        File source = (File) data.get("coremodLocation");
        if (source == null) { // this is usually in a dev env
            try {
                source = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to acquire source location for SevenCommons!", e);
            }
        }
        // cant set this directly, we cannot reference anybody else
        Launch.blackboard.put("__sevencommons.source", source);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
