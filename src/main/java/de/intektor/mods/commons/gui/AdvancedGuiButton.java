package de.intektor.mods.commons.gui;

import net.minecraft.client.gui.GuiButton;

/**
 * 
 * @author Intektor
 *
 */
public class AdvancedGuiButton extends GuiButton{

	int normalX;
	int normalY;
	public boolean atMoon;
	public AdvancedGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		normalX = x;
		normalY = y;
	}

	public void shootToMoon(){
		enabled = false;
		atMoon = true;
		this.xPosition = -100000;
		this.yPosition = -100000;
	}
	
	public void setNormal(){
		enabled = true;
		atMoon = false;
		xPosition = normalX;
		yPosition = normalY;
	}
}
