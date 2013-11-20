package net.minecraft.client.gui;

public final class SCGuiScreenAccessor {

	private SCGuiScreenAccessor() { }
	
	public static void actionPerfomed(GuiScreen gui, GuiButton btn) {
		gui.actionPerformed(btn);
	}
	
}
