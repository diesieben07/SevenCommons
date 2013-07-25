package de.take_weiland.mods.commons.internal.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;

import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersionInfo.ModVersion;
import de.take_weiland.mods.commons.internal.updater.ModVersionInfo;
import de.take_weiland.mods.commons.internal.updater.ModsFolderMod;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.internal.updater.UpdateStateListener;

public class GuiUpdates extends GuiScreen implements UpdateStateListener {

	private static final int BUTTON_BACK = 0;
	private static final int BUTTON_SEARCH = 1;
	private static final int BUTTON_UPDATE = 2;
	private static final int BUTTON_VERSION = 3;
	
	private final GuiScreen parent;
	private final UpdateController controller;
	private GuiSlotUpdateList scroller;
	final List<ModsFolderMod> mods;
	
	private GuiButton buttonCheckUpdates;
	private GuiButton buttonDownloadUpdate;
	private GuiButton buttonVersionSelect;
	
	private int downloadProgress;
	private int downloadTotal = -1;
	
	int selectedIndex = -1;
	
	int selectedVersionIndex;
	ModVersion selectedVersion = null;
	
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
		buttonList.add((buttonVersionSelect = new GuiButton(BUTTON_VERSION, width - btnWidth - 20, height - 141, btnWidth, 20, "")));
		
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
		if (selectedIndex == index) {
			int percent = -1;
			int scaled = 0;
			synchronized (this) {
				if (downloadTotal > 0) {
					percent = (int) ((100F / downloadTotal) * downloadProgress);
					scaled = (int) ((139F / downloadTotal) * downloadProgress);
				}
			}
			if (percent > 0) {
				drawRect(12, yPos + 22, 12 + scaled, yPos + 31, 0xff00ff00);
				drawCenteredString(fontRenderer, percent + "%", 12 + 139 / 2, yPos + 22, 0xffffff);
			}
		}
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
		UpdatableMod mod;
		super.actionPerformed(button);
		switch (button.id) {
		case BUTTON_BACK:
			close();
			break;
		case BUTTON_SEARCH:
			controller.searchForUpdates(mods.get(selectedIndex));
			break;
		case BUTTON_UPDATE:
			mod = mods.get(selectedIndex);
			controller.update(mod, mod.getVersionInfo().getNewestInstallableVersion());
			break;
		case BUTTON_VERSION:
			mod = mods.get(selectedIndex);
			selectedVersionIndex++;
			if (selectedVersionIndex >= mod.getVersionInfo().getAvailableVersions().size()) {
				selectedVersionIndex = 0;
			}
			selectedVersion = mod.getVersionInfo().getAvailableVersions().get(selectedVersionIndex);
			updateButtonState();
			break;
		}
	}

	void clickSlot(int index, boolean doubleClick) {
		selectedIndex = index;
		selectedVersion = null;
		
		synchronized (this) {
			downloadTotal = -1;
		}
		updateButtonState();
	}
	
	private void updateButtonState() {
		UpdatableMod mod = selectedIndex >= 0 ? mods.get(selectedIndex) : null;
		if (selectedIndex >= 0) {
			ModUpdateState state = mod.getState();
			buttonCheckUpdates.enabled = state.canTransition(ModUpdateState.CHECKING);
			buttonDownloadUpdate.enabled = state.canTransition(ModUpdateState.DOWNLOADING);
			
			if (selectedVersion == null) {
				ModVersionInfo versions = mod.getVersionInfo();
				if (versions != null) {
					selectedVersion = versions == null ? null : versions.getNewestInstallableVersion();
				} else {
					selectedVersion = null;
				}
			}
		}
		
		buttonCheckUpdates.drawButton = buttonDownloadUpdate.drawButton = selectedIndex >= 0;
		
		buttonVersionSelect.drawButton = selectedIndex >= 0 && selectedVersion != null;
		
		if (selectedVersion != null) {
			String format;
			EnumChatFormatting color;
			if (selectedVersion == mod.getVersionInfo().getNewestInstallableVersion()) {
				color = EnumChatFormatting.DARK_GREEN;
				format = I18n.func_135053_a("sevencommons.ui.updates.newest");
			} else if (selectedVersion.canBeInstalled()) {
				color = EnumChatFormatting.YELLOW;
				format = I18n.func_135053_a("sevencommons.ui.updates.installable");
			} else {
				color = EnumChatFormatting.RED;
				format = I18n.func_135053_a("sevencommons.ui.updates.wrongmc");
			}
			buttonVersionSelect.displayString = String.format(color + format, EnumChatFormatting.RESET + selectedVersion.modVersion.toString() + color, selectedVersion.minecraftVersion);
		}
	}
	
	private void close() {
		mc.displayGuiScreen(parent);
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		if (selectedIndex >= 0 && mod == mods.get(selectedIndex)) {
			updateButtonState();
		}
	}

	@Override
	public void onDownloadProgress(UpdatableMod mod, int progress, int total) {
		if (selectedIndex >= 0 && mod == mods.get(selectedIndex)) {
			synchronized (this) {
				if (total == progress) {
					downloadTotal = -1;
				} else {
					downloadTotal = total;
					downloadProgress = progress;
				}
			}
		}
	}
}
