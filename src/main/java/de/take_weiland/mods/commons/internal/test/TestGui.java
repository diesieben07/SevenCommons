package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.client.AbstractGuiContainer;
import net.minecraft.util.ResourceLocation;

/**
 * @author diesieben07
 */
public class TestGui extends AbstractGuiContainer<TestContainer> {
    public TestGui(TestContainer container) {
        super(container);
    }

    @Override
    public void updateScreen() {

    }

    @Override
    protected ResourceLocation provideTexture() {
        return null;
    }
}
