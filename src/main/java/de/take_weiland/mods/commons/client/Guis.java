package de.take_weiland.mods.commons.client;

import net.minecraft.client.Minecraft;

/**
 * <p>Utilities for working with {@code GuiScreens}</p>
 *
 * @author diesieben07
 * @see net.minecraft.client.gui.GuiScreen
 */
public final class Guis {

	private Guis() {
	}

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
	 * <p>Computes the current GUI scale. Calling this method is equivalent to the following:</p>
	 * <p><pre>{@code
	 * Minecraft mc = Minecraft.getMinecraft();
	 * int scale = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight).getScaleFactor(); }</pre></p>
	 *
	 * @return the current GUI scale
	 */
	public static int computeGuiScale() {
		Minecraft mc = Minecraft.getMinecraft();
		int scaleFactor = 1;

		int k = mc.gameSettings.guiScale;

		if (k == 0) {
			k = 1000;
		}

		while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
			++scaleFactor;
		}
		return scaleFactor;
	}

}
