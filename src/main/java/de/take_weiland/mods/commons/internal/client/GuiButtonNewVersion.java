package de.take_weiland.mods.commons.internal.client;

import de.take_weiland.mods.commons.client.Guis;
import de.take_weiland.mods.commons.client.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * @author diesieben07
 */
public class GuiButtonNewVersion extends GuiButton {

	String versionInfo = "";

	public GuiButtonNewVersion(int id, int x, int y, int width, int height, String text) {
		super(id, x, y, width, height, text);
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		if (drawButton && enabled && field_82253_i /* isHovered */) {
			int pos = mouseX + 10;
			int width = mc.fontRenderer.getStringWidth(versionInfo);
			int rightmost = pos + width;
			if (rightmost * Guis.computeGuiScale() > mc.displayWidth) {
				pos -= (15 + width);
			}
			Rendering.drawColoredRect(pos, mouseY, width + 4, 13, 0x000000, 0xDD);
			mc.fontRenderer.drawString(versionInfo, pos + 2, mouseY + 2, 0xffffff);
		}
	}
}
