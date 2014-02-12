package de.take_weiland.mods.commons.internal.client;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.internal.PacketUpdateAction;
import de.take_weiland.mods.commons.internal.PacketUpdateAction.Action;
import de.take_weiland.mods.commons.internal.updater.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;

public class GuiUpdates extends GuiScreen implements UpdateStateListener {

	private static final int BUTTON_BACK = 0;
	private static final int BUTTON_SEARCH = 1;
	private static final int BUTTON_UPDATE = 2;
	private static final int BUTTON_VERSION = 3;
	private static final int BUTTON_RESTART = 4;
	
	private final String textCheckUpdates = I18n.getString("sevencommons.ui.updates.check");
	private final String textChecking = I18n.getString("sevencommons.ui.updates.checking");
	private final String textUpdate = I18n.getString("sevencommons.ui.updates.update");
	private final String textPatchNotes = I18n.getString("sevencommons.ui.updates.patchnotes");
	private final String textNoPatchNotes = I18n.getString("sevencommons.ui.updates.patchnotes.none");
	private final String textCurrentVersion = I18n.getString("sevencommons.ui.updates.version.current");
	private final String textNewVersion = I18n.getString("sevencommons.ui.updates.version.new");
	private final String textRestart = I18n.getString("sevencommons.ui.updates.restart");
	
	private final GuiScreen parent;
	final UpdateController controller;
	private GuiSlotUpdateList scroller;
	final List<UpdatableMod> mods;
	
	private int rightButtonsWidth;
	private GuiButton buttonCheckUpdates;
	private GuiButton buttonDownloadUpdate;
	private GuiButton buttonVersionSelect;
	private GuiButton buttonRestartMinecraft;
	
	int selectedIndex = -1;
	private UpdatableMod selectedMod;
	
	public GuiUpdates(GuiScreen parent, UpdateController controller) {
		this.parent = parent;
		this.controller = controller;
		mods = Lists.newArrayList(controller.getMods());
		
		Collections.sort(mods, UpdatableMod.ACTIVE_AND_NAME_ORDER);
		
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
		
		buttonList.add(new GuiButton(BUTTON_BACK, width / 2 - 50, height - 40, 100, 20, "Back"));
		
		rightButtonsWidth = Math.max(fontRenderer.getStringWidth(textCheckUpdates), fontRenderer.getStringWidth(textUpdate));
		rightButtonsWidth = Math.max(rightButtonsWidth, fontRenderer.getStringWidth(textChecking));
		rightButtonsWidth = Math.max(rightButtonsWidth, fontRenderer.getStringWidth(textRestart)) + 10;
		
		buttonList.add((buttonCheckUpdates = new GuiButton(BUTTON_SEARCH, width - rightButtonsWidth - 20, height - 40, rightButtonsWidth, 20, "")));
		buttonList.add((buttonDownloadUpdate = new GuiButton(BUTTON_UPDATE, width - rightButtonsWidth - 20, height - 65, rightButtonsWidth, 20, textUpdate)));
		buttonList.add((buttonVersionSelect = new GuiButton(BUTTON_VERSION, width - rightButtonsWidth - 20, height - 90, rightButtonsWidth, 20, "")));
		
		buttonList.add((buttonRestartMinecraft = new GuiButton(BUTTON_RESTART, width - rightButtonsWidth - 20, height - 140, rightButtonsWidth, 20, textRestart)));
		
		updateButtonState();
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		controller.unregisterListener(this);
		new PacketUpdateAction(Action.CLOSE_SCREEN).sendToServer();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		scroller.drawScreen(mouseX, mouseY, par3);

		drawCenteredString(fontRenderer, "Mod Updates", width / 2, 10, 0xffffff);
		
		if (selectedMod != null) {
			ModUpdateState state = selectedMod.getState();
			
			fontRenderer.drawSplitString(state.getLongDescription(), width - rightButtonsWidth - 20, 32, rightButtonsWidth, 0xffffff);
			
			boolean canDownload = state.canTransition(ModUpdateState.DOWNLOADING);
			
			ModVersionCollection versions = selectedMod.getVersions();
			String curr = versions.getCurrentVersion().modVersion.getVersionString();
			fontRenderer.drawString(String.format(textCurrentVersion, curr), width - rightButtonsWidth - 20, height - 114, 0xffffff);
			
			if (canDownload) {
				fontRenderer.drawString(textNewVersion, width - rightButtonsWidth - 20, height - 102, 0xffffff);

				ModVersion version = versions.getSelectedVersion();
				
				if (version != null) {
					boolean hasNotes = !Strings.isNullOrEmpty(version.patchNotes);
					fontRenderer.drawString(hasNotes ? textPatchNotes : textNoPatchNotes, 170, 32, 0xffff00);
					if (hasNotes) {
						fontRenderer.drawSplitString(version.patchNotes, 170, 50, width - rightButtonsWidth - 190, 0xDDDDDD);
					}
				}
			}
		}
		super.drawScreen(mouseX, mouseY, par3);
	}
	
	void drawSlot(int index, int yPos) {
		UpdatableMod mod = mods.get(index);
		fontRenderer.drawString(mod.getName(), 12, yPos, 0xffffff);
		
		ModUpdateState state = mod.getState();
		fontRenderer.drawString(state.getDescriptionColor() + state.getShortDescription(), 12, yPos + 11, 0xffffff);
		
		int percent = mod.getDowloadProgress(100);
		if (percent > 0) {
			int scaled = mod.getDowloadProgress(139);
		
			drawRect(12, yPos + 22, 12 + scaled, yPos + 31, 0xff00ff00);
			drawCenteredString(fontRenderer, percent + "%", 12 + 139 / 2, yPos + 22, 0xffffff);
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
		super.actionPerformed(button);
		switch (button.id) {
		case BUTTON_BACK:
			close();
			break;
		case BUTTON_SEARCH:
			controller.searchForUpdates(selectedMod);
			break;
		case BUTTON_UPDATE:
			controller.update(selectedMod, selectedMod.getVersions().getSelectedVersion());
			break;
		case BUTTON_VERSION:
			selectedMod.getVersions().selectNextVersion();
			break;
		case BUTTON_RESTART:
			if (!controller.restartMinecraft()) {
				mc.displayGuiScreen(new GuiRestartFailure(this));
			}
			break;
		}
		updateButtonState();
	}
	
	void clickSlot(int index, boolean doubleClick) {
		selectedIndex = index;
		selectedMod = mods.get(index);
		updateButtonState();
	}
	
	private void updateButtonState() {
		buttonCheckUpdates.drawButton = buttonDownloadUpdate.drawButton = selectedIndex >= 0;
		buttonVersionSelect.drawButton = false;
		buttonRestartMinecraft.drawButton = false;
		
		if (selectedMod == null) {
			return;
		}
		
		ModVersion selectedVersion = selectedMod.getVersions().getSelectedVersion();
			
		if (selectedVersion != null) {
			String format;
			EnumChatFormatting color;
			if (selectedVersion == selectedMod.getVersions().getNewestInstallableVersion()) {
				color = EnumChatFormatting.DARK_GREEN;
				format = I18n.getString("sevencommons.ui.updates.newest");
			} else if (selectedVersion.canBeInstalled()) {
				color = EnumChatFormatting.YELLOW;
				format = I18n.getString("sevencommons.ui.updates.installable");
			} else if (selectedVersion.modVersion.getVersionString().equals(selectedMod.getVersions().getCurrentVersion().modVersion.getVersionString())) {
				color = EnumChatFormatting.BLUE;
				format = I18n.getString("sevencommons.ui.updates.installed");
			} else {
				color = EnumChatFormatting.RED;
				format = I18n.getString("sevencommons.ui.updates.wrongmc");
			}
			buttonVersionSelect.displayString = String.format(color + format, EnumChatFormatting.RESET + selectedVersion.modVersion.getVersionString() + color, selectedVersion.minecraftVersion);
		}
			
		ModUpdateState state = selectedMod.getState();
		buttonCheckUpdates.enabled = state.canTransition(ModUpdateState.CHECKING);
		buttonDownloadUpdate.enabled = state.canTransition(ModUpdateState.DOWNLOADING) && selectedVersion != null && selectedVersion.canBeInstalled();
		
		buttonVersionSelect.drawButton = !selectedMod.getVersions().getAvailableVersions().isEmpty() && state.canTransition(ModUpdateState.DOWNLOADING);
		
		buttonCheckUpdates.displayString = state == ModUpdateState.CHECKING ? textChecking : textCheckUpdates;
		buttonRestartMinecraft.drawButton = state == ModUpdateState.PENDING_RESTART;
	}
	
	private void close() {
		mc.displayGuiScreen(parent);
	}

	@Override
	public void onStateChange(UpdatableMod mod) {
		updateButtonState();
	}

	@Override
	public void onDownloadProgress(UpdatableMod mod) { }

}
