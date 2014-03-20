package net.minecraft.client.gui;

public final class SCGuiAccessor {

	private SCGuiAccessor() { }
	
	public static void actionPerformed(GuiScreen gui, GuiButton btn) {
		gui.actionPerformed(btn);
	}
	
	public static float getZLevel(Gui gui) {
		return gui.zLevel;
	}
	
}
