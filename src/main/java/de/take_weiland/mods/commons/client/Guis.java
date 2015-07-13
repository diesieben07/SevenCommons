package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.inv.Inventories;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.inventory.IInventory;

/**
 * <p>Utilities for working with {@code GuiScreens}</p>
 *
 * @author diesieben07
 * @see net.minecraft.client.gui.GuiScreen
 */
public final class Guis {

    /**
     * <p>Close any currently open GuiScreen.</p>
     */
    public static void close() {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    /**
     * <p>Determine whether the point with given {@code pointX} and {@code pointY} coordinates is in the rectangle at position {@code x, y} and with dimensions
     * {@code width} and {@code height}.</p>
     *
     * @param x      x position of the rectangle
     * @param y      y position of the rectangle
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param pointX x position of the point to check
     * @param pointY y position of the point to check
     * @return whether the point lies within the bounds
     */
    public static boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    /**
     * <p>Computes the current GUI scale. Calling this method is equivalent to the following:<pre><code>
     * Minecraft mc = Minecraft.getMinecraft();
     * int scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();</code></pre></p>
     *
     * @return the current GUI scale
     */
    public static int computeGuiScale() {
        Minecraft mc = Minecraft.getMinecraft();
        return new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
    }

    /**
     * <p>Draw the name for the given inventory at the given coordinates in the default color (<code>0x404040</code>).</p>
     *
     * @param inventory the inventory
     * @param x         the x coordinate
     * @param y         the y coordinate
     */
    public static void drawInventoryName(IInventory inventory, int x, int y) {
        drawInventoryName(inventory, x, y, 0x404040);
    }

    /**
     * <p>Draw the name for the given inventory at the given coordinates in the given color.</p>
     *
     * @param inventory the inventory
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param color     the color
     */
    public static void drawInventoryName(IInventory inventory, int x, int y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawString(Inventories.getDisplayName(inventory), x, y, color);
    }

    private Guis() {
    }

}
