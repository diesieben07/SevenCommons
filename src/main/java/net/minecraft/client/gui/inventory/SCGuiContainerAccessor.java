package net.minecraft.client.gui.inventory;

public final class SCGuiContainerAccessor {

	private SCGuiContainerAccessor() { }

	public static int getSizeX(GuiContainer gui) {
		return gui.xSize;
	}
	
	public static int getSizeY(GuiContainer gui) {
		return gui.ySize;
	}
	
	public static int getGuiLeft(GuiContainer gui) {
		return gui.guiLeft;
	}
	
	public static int getGuiTop(GuiContainer gui) {
		return gui.guiTop;
	}
	
}
