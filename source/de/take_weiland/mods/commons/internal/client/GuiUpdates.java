package de.take_weiland.mods.commons.internal.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;

import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.internal.updater.UpdateStateListener;

public class GuiUpdates extends GuiScreen implements UpdateStateListener {

	private static final int BUTTON_BACK = 0;
	private static final int BUTTON_SEARCH = 1;
	private static final int BUTTON_UPDATE = 2;
	
	private final GuiScreen parent;
	private final UpdateController controller;
	private GuiSlotUpdateList scroller;
	final List<UpdatableMod> mods;
	
	private GuiButton buttonCheckUpdates;
	private GuiButton buttonDownloadUpdate;
	
	int selectedIndex = -1;
	
	public GuiUpdates(GuiScreen parent, UpdateController controller) {
		this.parent = parent;
		this.controller = controller;
		mods = ImmutableList.copyOf(controller.getMods());
		controller.registerListener(this);
	}
	
	Minecraft getMinecraft() {
		return mc;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		scroller = new GuiSlotUpdateList(this);
		
		buttonList.add(new GuiButton(BUTTON_BACK, width / 2 - 100, height - 40, "Back"));
		
		String checkTxt = I18n.func_135053_a("sevencommons.ui.updates.check");
		String updateTxt = I18n.func_135053_a("sevencommons.ui.updates.update");
		int btnWidth = Math.max(fontRenderer.getStringWidth(checkTxt), fontRenderer.getStringWidth(updateTxt)) + 10;
		
		buttonList.add((buttonCheckUpdates = new GuiButton(BUTTON_SEARCH, width - btnWidth - 20, height - 81, btnWidth, 20, checkTxt)));
		buttonList.add((buttonDownloadUpdate = new GuiButton(BUTTON_UPDATE, width - btnWidth - 20, height - 111, btnWidth, 20, updateTxt)));
		
		updateButtonState();
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		controller.unregisterListener(this);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		scroller.drawScreen(par1, par2, par3);
		
		drawCenteredString(fontRenderer, "Mod Updates", width / 2, 10, 0xffffff);
		
		super.drawScreen(par1, par2, par3);
	}
	
	void drawSlot(int index, int yPos) {
		UpdatableMod mod = mods.get(index);
		fontRenderer.drawString(mod.getContainer().getName(), 12, yPos, 0xffffff);
		fontRenderer.drawString(mod.getState().toString(), 12, yPos + 11, 0xffffff);
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode) {
		super.keyTyped(keyChar, keyCode);
		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		switch (button.id) {
		case BUTTON_BACK:
			close();
		case BUTTON_SEARCH:
			controller.searchForUpdates(mods.get(selectedIndex));
			break;
		case BUTTON_UPDATE:
			UpdatableMod mod = mods.get(selectedIndex);
			controller.update(mod, mod.getVersionInfo().getNewestInstallableVersion());
			break;
		}
	}

	void clickSlot(int index, boolean doubleClick) {
		selectedIndex = index;
		updateButtonState();
	}
	
	private void updateButtonState() {
		buttonCheckUpdates.drawButton = buttonDownloadUpdate.drawButton = selectedIndex >= 0;
		if (selectedIndex >= 0) {
			UpdatableMod mod = mods.get(selectedIndex);
			ModUpdateState state = mod.getState();
			buttonCheckUpdates.enabled = state.canTransition(ModUpdateState.CHECKING);
			buttonDownloadUpdate.enabled = state.canTransition(ModUpdateState.DOWNLOADING);
		}
	}
	
	private void close() {
		mc.displayGuiScreen(parent);
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		if (mod == mods.get(selectedIndex)) {
			updateButtonState();
		}
	}
}
