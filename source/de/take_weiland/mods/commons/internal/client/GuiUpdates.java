package de.take_weiland.mods.commons.internal.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import de.take_weiland.mods.commons.internal.updater.ModUpdateState;
import de.take_weiland.mods.commons.internal.updater.ModVersionInfo.ModVersion;
import de.take_weiland.mods.commons.internal.updater.ModsFolderMod;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.internal.updater.UpdateStateListener;

public class GuiUpdates extends GuiScreen implements UpdateStateListener {

	private static final int BUTTON_BACK = 0;
	private static final int BUTTON_SEARCH = 1;
	private static final int BUTTON_UPDATE = 2;
	private static final int BUTTON_VERSION = 3;
	
	private final String textCheckUpdates = I18n.func_135053_a("sevencommons.ui.updates.check");
	private final String textChecking = I18n.func_135053_a("sevencommons.ui.updates.checking");
	private final String textUpdate = I18n.func_135053_a("sevencommons.ui.updates.update");
	
	private final GuiScreen parent;
	private final UpdateController controller;
	private GuiSlotUpdateList scroller;
	final List<ModsFolderMod> mods;
	
	private int leftButtonsWidth;
	private GuiButton buttonCheckUpdates;
	private GuiButton buttonDownloadUpdate;
	private GuiButton buttonVersionSelect;
	
	private int downloadProgress;
	private int downloadTotal = -1;
	
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
		
		leftButtonsWidth = Math.max(fontRenderer.getStringWidth(textCheckUpdates), fontRenderer.getStringWidth(textUpdate));
		leftButtonsWidth = Math.max(leftButtonsWidth, fontRenderer.getStringWidth(textChecking)) + 10;
		
		buttonList.add((buttonCheckUpdates = new GuiButton(BUTTON_SEARCH, width - leftButtonsWidth - 20, height - 81, leftButtonsWidth, 20, "")));
		buttonList.add((buttonDownloadUpdate = new GuiButton(BUTTON_UPDATE, width - leftButtonsWidth - 20, height - 111, leftButtonsWidth, 20, textUpdate)));
		buttonList.add((buttonVersionSelect = new GuiButton(BUTTON_VERSION, width - leftButtonsWidth - 20, height - 141, leftButtonsWidth, 20, "")));
		
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
		
		if (selectedIndex >= 0) {
			ModVersion version = getSelectedVersion(mods.get(selectedIndex));
			if (version != null && !Strings.isNullOrEmpty(version.patchNotes)) {
				fontRenderer.drawSplitString(version.patchNotes, 170, 32, width - leftButtonsWidth - 190, 0xDDDDDD);
			}
		}
		
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
			controller.update(mod, mod.getVersionInfo().getSelectedVersion());
			break;
		case BUTTON_VERSION:
			mods.get(selectedIndex).getVersionInfo().selectNextVersion();
			break;
		}
		updateButtonState();
	}

	void clickSlot(int index, boolean doubleClick) {
		selectedIndex = index;
		
		synchronized (this) {
			downloadTotal = -1;
		}
		updateButtonState();
	}
	
	private void updateButtonState() {
		UpdatableMod mod = selectedIndex >= 0 ? mods.get(selectedIndex) : null;
		
		buttonCheckUpdates.drawButton = buttonDownloadUpdate.drawButton = selectedIndex >= 0;
		buttonVersionSelect.drawButton = selectedIndex >= 0 && mod.getVersionInfo() != null;
		
		if (selectedIndex >= 0) {
			ModVersion selectedVersion = getSelectedVersion(mod);
			
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
			
			ModUpdateState state = mod.getState();
			buttonCheckUpdates.enabled = state.canTransition(ModUpdateState.CHECKING);
			buttonDownloadUpdate.enabled = state.canTransition(ModUpdateState.DOWNLOADING) && selectedVersion != null && selectedVersion.canBeInstalled();
			
			buttonVersionSelect.drawButton = buttonVersionSelect.drawButton && state.canTransition(ModUpdateState.DOWNLOADING);
			
			buttonCheckUpdates.displayString = state == ModUpdateState.CHECKING ? textChecking : textCheckUpdates;
		}
	}
	
	private static ModVersion getSelectedVersion(UpdatableMod mod) {
		return mod.getVersionInfo() != null ? mod.getVersionInfo().getSelectedVersion() : null;
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
