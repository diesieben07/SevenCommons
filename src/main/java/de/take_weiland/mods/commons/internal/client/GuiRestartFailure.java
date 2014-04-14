package de.take_weiland.mods.commons.internal.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public final class GuiRestartFailure extends GuiScreen {

	private final GuiScreen parent;
	
	private final String textOk = I18n.getString("sevencommons.ui.ok");

	public GuiRestartFailure(GuiScreen currentScreen) {
		parent = currentScreen;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTickTime) {
		drawBackground(0);
		super.drawScreen(mouseX, mouseY, partialTickTime);
		
		fontRenderer.drawSplitString("Failed to restart Minecraft automatically.\n"
				+ "This technique is experimental and may not always work. To increase chances, please use Oracles VM.\n\n"
				+ "Please restart Minecraft manually.", 20, 20, width - 40, 0xffffff);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		buttonList.add(new GuiButton(0, width / 2 - 40, height - 40, 80, 20, textOk));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		mc.displayGuiScreen(parent);
	}
}
