package de.take_weiland.mods.commons.inv;

import net.minecraft.client.gui.GuiButton;

/**
 * A GuiButton with some more features
 * @author Intektor
 */
public class SimpleGuiButton extends GuiButton {

    /**
     * Normal position
     */
    private final int normalX, normalY;

    public SimpleGuiButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
        normalX = x;
        normalY = y;
    }

    public SimpleGuiButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
        normalX = x;
        normalY = y;
    }

    /**
     * Hides the button from the player, it can't be clicked anymore
     */
    public void hideButton() {
        setPosition(-100000, -100000);
    }

    /**
     * Sets the render position of the button
     * @param x
     * @param y
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Removes the button back to the original position
     */
    public void setNormalPosition() {
        setPosition(normalX, normalY);
    }
}
