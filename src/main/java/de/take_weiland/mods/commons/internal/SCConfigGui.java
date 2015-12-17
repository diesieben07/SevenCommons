package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.List;
import java.util.Set;

/**
 * @author diesieben07
 */
public class SCConfigGui extends GuiConfig {

    public SCConfigGui(GuiScreen parent) {
        super(parent,
                createConfigElements(),
                SevenCommons.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(SevenCommons.config.toString()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<IConfigElement> createConfigElements() {
        return ImmutableList.<IConfigElement>builder()
                .addAll(new ConfigElement(SevenCommons.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements())
                .build();
    }

    public static final class Factory implements IModGuiFactory {

        @Override
        public void initialize(Minecraft mc) {

        }

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return SCConfigGui.class;
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return null;
        }

        @Override
        public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
            return null;
        }

    }

}
