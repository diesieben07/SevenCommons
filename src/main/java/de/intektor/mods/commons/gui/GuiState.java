package de.intektor.mods.commons.gui;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * @author Intektor
 *
 */

public class GuiState {

	public ResourceLocation text;
	public ArrayList<AdvancedGuiButton> buttonList = new ArrayList<AdvancedGuiButton>();

	public GuiState(ResourceLocation texture, AdvancedGuiButton...buttonIDs) {	

		this.text = texture;
		for(int i = 0; i < buttonIDs.length; i++){
			buttonList.add(buttonIDs[i]);
		}
	}

	public void renderBackroundState(){
		Minecraft.getMinecraft().renderEngine.bindTexture(text);
	}

	public void renderScreen(){
		int k;

		for (k = 0; k < this.buttonList.size(); ++k)
		{
			((GuiButton)this.buttonList.get(k)).drawButton(Minecraft.getMinecraft(), Mouse.getX(), Mouse.getY());
		}
	}
}
