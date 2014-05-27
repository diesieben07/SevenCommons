package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

/**
 * Utilities for working with {@link net.minecraft.client.gui.GuiScreen GuiScreens}
 * @author diesieben07
 */
public final class Guis {

	private Guis() { }

	/**
	 * Copies the state from one Text field to the other. Useful to preserve state when the resolution changes.
	 * @param from the text field to copy from
	 * @param to the text field to copy to
	 */
	public static void copyState(GuiTextField from, GuiTextField to) {
		SCReflector reflector = SCReflector.instance;

		// i hope i didnt forget any
		to.setText(from.getText());
		to.setCursorPosition(from.getCursorPosition());
		to.setSelectionPos(from.getSelectionEnd());
		to.setFocused(from.isFocused());
		to.setEnabled(reflector.isEnabled(from));
		to.setMaxStringLength(from.getMaxStringLength());
		to.setCanLoseFocus(reflector.canLooseFocus(from));
		to.setEnableBackgroundDrawing(from.getEnableBackgroundDrawing());
		to.setTextColor(reflector.getEnabledColor(from));
		to.setDisabledTextColour(reflector.getDisabledColor(from));
		to.setVisible(from.getVisible());
	}

	/**
	 * closes any currently open GuiScreen.
	 */
	public static void close() {
		Minecraft.getMinecraft().displayGuiScreen(null);
	}

	/**
	 * determines whether the point with given {@code pointX} and {@code pointY} coordinates is in the rectangle at position {@code x, y} and with dimensions
	 * {@code width x height}
	 * @param x x position of the rectangle
	 * @param y y position of the rectangle
	 * @param width width of the rectangle
	 * @param height height of the rectangle
	 * @param pointX x position of the point to check
	 * @param pointY y position of the point to check
	 * @return whether the point lies within the bounds
	 */
	public static boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
		return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
	}

	/**
	 * computes the current GUI scale. Calling this method is equivalent to<br />
	 * <pre>{@code
	 * Minecraft mc = Minecraft.getMinecraft();
	 * int scale = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight).getScaleFactor(); }</pre>
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
