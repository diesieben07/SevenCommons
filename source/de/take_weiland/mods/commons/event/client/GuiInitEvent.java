package de.take_weiland.mods.commons.event.client;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.event.Event;

public class GuiInitEvent extends Event {

	public final GuiScreen gui;
	public final List<GuiButton> buttons;
	
	public GuiInitEvent(GuiScreen gui, List<GuiButton> buttons) {
		this.gui = gui;
		this.buttons = buttons;
	}

}
