package de.take_weiland.mods.commons.internal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.take_weiland.mods.commons.internal.CommonsModContainer;


public class GuiButtonUpdates extends GuiButton {

	public static final ResourceLocation texture = new ResourceLocation("sevencommons:gui.png");
	
	public GuiButtonUpdates(int id, int x, int y) {
		super(id, x, y, 20, 20, "");
		if (CommonsModContainer.updateController == null) {
			throw new IllegalStateException("Update Button should not exist when updateController is null!");
		}
		if (!CommonsModContainer.updaterEnabled) {
			throw new IllegalStateException("Update Button should not exist when updates are disabled!");
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			mc.displayGuiScreen(new GuiUpdates(mc.currentScreen, CommonsModContainer.updateController));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		super.drawButton(mc, mouseX, mouseY);
		if (drawButton) {
			GL11.glColor3f(1, 1, 1);
			mc.renderEngine.bindTexture(texture);
			drawTexturedModalRect(xPosition + 2, yPosition + 2, 0, 0, 16, 16);
		}
	}
}
