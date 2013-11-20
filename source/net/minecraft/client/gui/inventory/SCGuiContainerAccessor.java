package net.minecraft.client.gui.inventory;

public final class SCGuiContainerAccessor {

	private SCGuiContainerAccessor() { }

	public static int getSizeX(GuiContainer gui) {
		return gui.xSize;
	}
	
	public static int getSizeY(GuiContainer gui) {
		return gui.ySize;
	}
	
}
