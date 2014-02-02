package de.take_weiland.mods.commons.client;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

public abstract class GuiScreenWithParent extends GuiScreen {

	protected final GuiScreen parent;
	
	public GuiScreenWithParent(GuiScreen parent) {
		this.parent = parent;
	}
	
	protected final void close() {
		mc.displayGuiScreen(parent);
	}

	@Override
	protected void keyTyped(char c, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
		}
	}
	
}
