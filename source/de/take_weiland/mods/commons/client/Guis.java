package de.take_weiland.mods.commons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public final class Guis {

	private Guis() { }

	public static void copyState(GuiTextField from, GuiTextField to) {
		to.setText(from.getText());
		to.setCursorPosition(from.getCursorPosition());
		to.setSelectionPos(from.getSelectionEnd());
		to.setFocused(from.isFocused());
	}
	
	public static void close() {
		Minecraft.getMinecraft().displayGuiScreen(null);
	}
	
	public static boolean isPointInRegion(int x, int y, int width, int height, int pointX, int pointY) {
		return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
	}
	
	public static int computeGuiScale(Minecraft mc) {
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
