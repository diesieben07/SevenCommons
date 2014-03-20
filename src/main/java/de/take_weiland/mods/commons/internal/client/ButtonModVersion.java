package de.take_weiland.mods.commons.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * @author diesieben07
 */
public class ButtonModVersion extends GuiButton {

	private final Minecraft mc = Minecraft.getMinecraft();

	public ButtonModVersion(int id, int x, int y, String display) {
		super(id, x, y, 200, 9, display);
		calculateWidth();
	}

	public void setText(String txt) {
		this.displayString = txt;
		calculateWidth();
	}

	private void calculateWidth() {
		width = mc.fontRenderer.getStringWidth(displayString);
	}

}
