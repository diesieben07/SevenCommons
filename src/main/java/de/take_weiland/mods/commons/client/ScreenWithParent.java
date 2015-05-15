package de.take_weiland.mods.commons.client;

import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nullable;

/**
 * <p>Implement this interface on your GuiScreen to make it open a parent screen when it is closed by whatever means.</p>
 *
 * @author diesieben07
 */
public interface ScreenWithParent {

    /**
     * <p>The parent screen to open when this GuiScreen closes.</p>
     * <p>You can return null from this method to indicate that no screen should be opened.</p>
     *
     * @return the parent screen
     */
    @Nullable
    GuiScreen getParentScreen();

}
