package de.take_weiland.mods.commons.api.event.client;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.event.Event;

/**
 * called whenever {@link GuiScreen#initGui} gets called<br>
 * Usually used to add additional {@link GuiButton}s to a GuiScreen which is not yours
 * @author diesieben07
 *
 */
public class GuiInitEvent extends Event {

	/**
	 * the GuiScreen being initialized
	 */
	public final GuiScreen gui;
	
	/**
	 * the buttonList of this GuiScreen<br>
	 * You may add to this
	 */
	public final List<GuiButton> buttons;
	
	public GuiInitEvent(GuiScreen gui, List<GuiButton> buttons) {
		this.gui = gui;
		this.buttons = buttons;
	}

}
