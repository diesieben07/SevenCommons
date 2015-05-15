package de.take_weiland.mods.commons.client;

import net.minecraft.client.gui.GuiScreen;

/**
 * <p>Abstract base class for GuiScreens which have a parent screen that should be reopened when this screen closes.</p>
 *
 * @author diesieben07
 */
public abstract class GuiScreenWithParent extends GuiScreen implements ScreenWithParent {

    /**
     * <p>The parent screen</p>
     */
    protected final GuiScreen parent;

    /**
     * <p>Create a new GuiScreenWithParent.</p>
     *
     * @param parent the parent screen
     */
    public GuiScreenWithParent(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public final GuiScreen getParentScreen() {
        return parent;
    }

}
