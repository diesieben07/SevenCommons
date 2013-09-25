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
	
}
