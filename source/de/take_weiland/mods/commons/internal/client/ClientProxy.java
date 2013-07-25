package de.take_weiland.mods.commons.internal.client;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.event.ForgeSubscribe;
import de.take_weiland.mods.commons.event.client.GuiInitEvent;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;

public class ClientProxy implements SevenCommonsProxy {

	@ForgeSubscribe
	public void onGuiInit(GuiInitEvent event) {
		if (event.gui instanceof GuiMainMenu) {
			event.buttons.add(new GuiButtonUpdates(-1, event.gui.width / 2 + 104, event.gui.height / 4 + 48 + 72 + 12));
		}
	}

}
