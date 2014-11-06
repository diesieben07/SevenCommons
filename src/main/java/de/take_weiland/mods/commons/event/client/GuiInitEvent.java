package de.take_weiland.mods.commons.event.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.event.Event;

import java.util.List;

/**
 * <p>Called whenever {@link GuiScreen#initGui} gets called.</p>
 * <p>Usually used to add additional buttons to a GuiScreen which is not yours.</p>
 * <p>To be replaced by the forge version in 1.7.</p>
 *
 * @author diesieben07
 */
public final class GuiInitEvent extends Event {

	/**
	 * <p>The GuiScreen.</p>
	 */
	public final GuiScreen gui;

	/**
	 * <p>The buttons in the GuiScreen, you can modify this list.</p>
	 */
	public final List<GuiButton> buttons;

	public GuiInitEvent(GuiScreen gui, List<GuiButton> buttons) {
		this.gui = gui;
		this.buttons = buttons;
	}

}
