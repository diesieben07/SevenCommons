package de.take_weiland.mods.commons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

public abstract class SliderButton extends GuiButton {

	private boolean dragging;

	private float value;

	public SliderButton(int id, int x, int y, String text, float value) {
		this(id, x, y, 150, 20, text, value);
	}


	public SliderButton(int id, int x, int y, int width, int height, String text, float value) {
		super(id, x, y, width, height, text);
		this.value = value;
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (drawButton) {
			if (dragging) {
				updateValue(mouseX);
			}

			GL11.glColor3f(1, 1, 1);
			this.drawTexturedModalRect(xPosition + (int) (value * (float) (width - 8)), yPosition, 0, 66, 4, 20);
			this.drawTexturedModalRect(xPosition + (int) (value * (float) (width - 8)) + 4, yPosition, 196, 66, 4, 20);
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			updateValue(mouseX);

			dragging = true;
			return true;
		} else {
			return false;
		}
	}


	private void updateValue(int mouseX) {
		value = (float) (mouseX - (xPosition + 4)) / (float) (width - 8);

		if (value < 0) {
			value = 0;
		}

		if (value > 1) {
			value = 1;
		}

		newValue(value);
	}

	protected abstract void newValue(float value);

	@Override
	public void mouseReleased(int par1, int par2) {
		this.dragging = false;
	}

	@Override
	protected int getHoverState(boolean whatever) {
		return 0;
	}

}
