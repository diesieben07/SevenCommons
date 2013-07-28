package de.take_weiland.mods.commons.internal.client;

import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.client.GuiScrollingList;

public class GuiSlotUpdateList extends GuiScrollingList {

	private final GuiUpdates parent;
	
	public GuiSlotUpdateList(GuiUpdates parent) {
		super(parent.getMinecraft(), 150, parent.height, 32, parent.height - 61, 10, 35);
		this.parent = parent;
	}

	@Override
	protected int getSize() {
		return parent.mods.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		parent.clickSlot(index, doubleClick);
	}

	@Override
	protected boolean isSelected(int index) {
		return parent.selectedIndex == index;
	}

	@Override
	protected void drawBackground() {
		parent.drawBackground(0);
	}

	@Override
	protected void drawSlot(int index, int var2, int yPos, int var4, Tessellator var5) {
		parent.drawSlot(index, yPos);
	}

}
