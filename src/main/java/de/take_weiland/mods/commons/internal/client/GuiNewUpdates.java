package de.take_weiland.mods.commons.internal.client;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.client.GuiScreenWithParent;
import de.take_weiland.mods.commons.client.Guis;
import de.take_weiland.mods.commons.client.Rendering;
import de.take_weiland.mods.commons.client.ScrollPane;
import de.take_weiland.mods.commons.internal.PacketUpdaterAction;
import de.take_weiland.mods.commons.internal.updater.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

/**
 * @author diesieben07
 */
public class GuiNewUpdates extends GuiScreenWithParent {

	private static final int BUTTON_INSTALL = 0;
	private static final int BUTTON_OPTIMIZE = 1;
	private static final int BUTTON_RECHECK = 2;
	private static final int BUTTON_RESTART = 3;

	private static final EnumChatFormatting INSTALLED = DARK_AQUA;
	private static final EnumChatFormatting AVAILABLE = DARK_GREEN;
	private static final EnumChatFormatting UNAVAILABLE = RED;

	private static final int BUTTON_Y = 25;
	private static final int HEADER_Y = BUTTON_Y + 30;
	private static final int MOD_HEIGHT = 11;
	private static final int VERSIONS_Y_START = HEADER_Y + 11;

	private static final int SCROLLER_X = 13;
	private static final int SCROLLER_Y = HEADER_Y + 15;

	private static final int TICKBOX_SIZE = 7;
	private static final int MODS_START_X = 0;

	UpdateController controller;
	private List<? extends UpdatableMod> mods;
	private GuiButton buttonInstall;
	private GuiButton buttonRefresh;
	private GuiButton buttonOptimize;
	private GuiButton buttonRestart;

	private int installButtonWidth;

	private int modListLastY;
	private int markedModCheckboxXStart;
	private UpdatableMod markedMod;
	boolean showOptimizeError = false;
	private String headerText;
	private int headerWidth;
	private int scrollerWidth;
	private ModScroller scroller;

	int ticks = 0;

	public GuiNewUpdates(GuiScreen parent, UpdateController controller) {
		super(parent);
		injectNewController(controller);
	}

	private static String translate(String s) {
		return I18n.getString("sevencommons.updates." + s);
	}

	private static String translate(String s, Object... args) {
		return I18n.getStringParams("sevencommons.updates." + s, args);
	}

	void injectNewController(UpdateController controller) {
		this.controller = controller;
		mods = ModSorter.INSTANCE.immutableSortedCopy(controller.getMods());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		String txtInstall = translate("install");
		int widthInstallRestart = fontRenderer.getStringWidth(txtInstall);

		String txtRestart = translate("restart");
		widthInstallRestart = Math.max(widthInstallRestart, fontRenderer.getStringWidth(txtRestart));

		installButtonWidth = widthInstallRestart + 6;
		String txtOptimize = translate("optimize");
		int widthOptimize = fontRenderer.getStringWidth(txtOptimize);
		String txtRecheck = translate("recheck");
		int widthRecheck = fontRenderer.getStringWidth(txtRecheck);

		buttonList.add((buttonInstall = new GuiButton(BUTTON_INSTALL, 10, BUTTON_Y, widthInstallRestart + 6, 20, txtInstall)));
		buttonList.add((buttonRestart = new GuiButton(BUTTON_RESTART, 10, BUTTON_Y, widthInstallRestart + 6, 20, txtRestart)));

		buttonList.add((buttonOptimize = new GuiButton(BUTTON_OPTIMIZE, 20 + widthInstallRestart, BUTTON_Y, widthOptimize + 6, 20, txtOptimize)));
		buttonList.add((buttonRefresh = new GuiButton(BUTTON_RECHECK, 30 + widthInstallRestart + widthOptimize, BUTTON_Y, widthRecheck + 6, 20, txtRecheck)));

		scroller = new ModScroller(SCROLLER_X, SCROLLER_Y, 150, height - SCROLLER_Y - 10, 0);
		scroller.setClip(true);
		headerText = translate("available");
		headerWidth = fontRenderer.getStringWidth(headerText);

		checkingMarkerWidth = fontRenderer.getStringWidth(CHECKING_MARKER) + 2;

		updateButtonState();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float renderTime) {
		drawBackground(0);

		if (showOptimizeError || controller.hasFailed()) {
			mouseX = mouseY = -10;
		}

		super.drawScreen(mouseX, mouseY, renderTime);

//		System.out.println("Pending: " + controller.isRestartPending() + ", installing: " + controller.isInstalling());
		if (!controller.isRestartPending() && controller.isInstalling()) {
			int percent = controller.getDownloadPercent();
			int width = installButtonWidth * percent / 100;

			Rendering.horizontalGradient(10, BUTTON_Y + 2, width, 16, 0x007700, 0x00ff00);

			String s = percent + " %";
			int sWidth = fontRenderer.getStringWidth(s);
			fontRenderer.drawString(s, 10 + (installButtonWidth - sWidth) / 2, BUTTON_Y + 6, 0xffffff);
		}

		drawCenteredString(fontRenderer, translate("header"), width / 2, 10, 0xffffff);

		fontRenderer.drawString(UNDERLINE + headerText, 10, HEADER_Y, 0xffffff);

		scroller.updateHeight();
		scroller.draw(mouseX, mouseY);

		if (markedMod != null) {
			UpdatableMod mod = markedMod;
			int modInfoX = MODS_START_X + scrollerWidth + 15;
			String s = translate("modInfo", DARK_GREEN + mod.getName());
			fontRenderer.drawString(s, modInfoX, HEADER_Y, 0xffffff);
			s = translate("availableVersions");
			int width = fontRenderer.getStringWidth(s);
			fontRenderer.drawString(s, modInfoX, VERSIONS_Y_START, 0xffffff);
			int y = VERSIONS_Y_START;
			int x = markedModCheckboxXStart = modInfoX + width + 3;
			ModVersion selected = mod.getVersions().getSelectedVersion();
			ModVersion mouseOverVersion = null;
			int mouseOverPos = -1;
			for (ModVersion version : mod.getVersions().getAvailableVersions()) {
				CheckboxState state = version == selected ? CheckboxState.SELECTED : (version.isInstalled() ? CheckboxState.INSTALLED : CheckboxState.NONE);
				drawTickbox(x, y, state);
				String versionDisplay = getVersionColor(version) + version.getVersionString();
				width = fontRenderer.getStringWidth(versionDisplay);
				fontRenderer.drawString(versionDisplay, x + TICKBOX_SIZE + 2, y, 0xffffff);
				if (mouseX >= x && mouseX <= x + width + TICKBOX_SIZE + 2 && mouseY >= y && mouseY < y + 10) {
					mouseOverPos = mouseX;
					mouseOverVersion = version;
				}

				y += 10;
			}
			if (mouseOverPos != -1) {
				drawModVersionMouseover(mouseOverVersion, mouseOverPos, mouseY);
			}
		}

		if (showOptimizeError) {
			drawOptimizeError();
		}
		if (controller.hasFailed()) {
			drawFailure();
		}
	}

	private void drawFailure() {
		String txt = translate("installFail");
		String txtReset = translate("failureReset");
		int txtWidth = fontRenderer.getStringWidth(txt);
		int txtResetWidth = fontRenderer.getStringWidth(txtReset);

		int boxWidth = Math.max(txtWidth, txtResetWidth) + 40;
		int boxX = (width - boxWidth) / 2;

		int txtX = (width - txtWidth) / 2;
		int txtResetX = (width - txtResetWidth) / 2;

		int y = 30;

		int height = 45 + controller.modsInState(ModUpdateState.INSTALL_FAIL) * 10;

		Rendering.drawColoredRect(boxX, y, boxWidth, height, 0x000000, 0xDD);

		y += 10;
		fontRenderer.drawString(txt, txtX, y, 0xffffff);
		y += 10;

		for (UpdatableMod mod : controller.getMods()) {
			if (mod.getState() == ModUpdateState.INSTALL_FAIL) {
				fontRenderer.drawString("- " + DARK_RED + mod.getName(), txtX + 4, y, 0xffffff);
				y += 10;
			}
		}
		y += 5;
		fontRenderer.drawString(txtReset, txtResetX, y, 0xffffff);
	}

	private void drawOptimizeError() {
		String txt = translate("optimizeFail");
		int txtWidth = fontRenderer.getStringWidth(txt) + 40;
		int x = (width - txtWidth) / 2;
		int y = 30;
		Rendering.drawColoredRect(x - 2, y - 2, txtWidth, 20, 0x000000, 0xDD);
		fontRenderer.drawString(txt, x + 20, y + 10, 0xffffff);
	}

	private EnumChatFormatting getVersionColor(ModVersion version) {
		return version.isInstalled() ? INSTALLED : (version.canBeInstalled() ? AVAILABLE : UNAVAILABLE);
	}

	private List<String> mouseOverBuf = Lists.newArrayList();
	private int mouseOverWidth;

	private void drawModVersionMouseover(ModVersion version, int x, int y) {
		mouseOverBegin();
		if (version.canBeInstalled()) {
			mouseOverAdd(AVAILABLE + translate("canInstall"));
		} else if (version.isInstalled()) {
			mouseOverAdd(INSTALLED + translate("installed"));
		} else {
			mouseOverAdd(UNAVAILABLE + translate("missingDeps"));
			for (Dependency dep : version.getDependencies()) {
				if (!dep.isSatisfied()) {
					mouseOverAdd(dep.getDisplay());
				}
			}
		}
		mouseOverDraw(x, y);
	}

	private void mouseOverAdd(String add) {
		mouseOverBuf.add(add);
		mouseOverWidth = Math.max(mouseOverWidth, fontRenderer.getStringWidth(add));
	}

	private void mouseOverBegin() {
		mouseOverBuf.clear();
		mouseOverWidth = 0;
	}

	private void mouseOverDraw(int x, int y) {
		int width = mouseOverWidth;
		int guiScale = Guis.computeGuiScale();
		x += 10;
		if ((x + width) * guiScale > mc.displayWidth) {
			x -= (15 + width);
		}
		int height = mouseOverBuf.size() * 10;
		if ((y + height) * guiScale > mc.displayHeight) {
			y -= (15 + height);
		}

		Rendering.drawColoredRect(x, y, width + 4, height + 3, 0x000000);

		y += 1;
		for (String s : mouseOverBuf) {
			fontRenderer.drawString(s, x + 2, y + 2, 0xffffff);
			y += 10;
		}
	}

	private String getDisplayString(UpdatableMod mod) {
		ModVersion version = mod.getVersions().getSelectedVersion();
		return mod.getName()
				+ ' '
				+ getVersionColor(version)
				+ version.getVersionString();
	}

	private void drawTickbox(int x, int y, CheckboxState state) {
		// top line
		Rendering.drawColoredRect(x, y, TICKBOX_SIZE, 1, 0xffffff);
		// bottom line
		Rendering.drawColoredRect(x, y + TICKBOX_SIZE - 1, TICKBOX_SIZE, 1, 0xffffff);

		// left line
		Rendering.drawColoredRect(x, y, 1, TICKBOX_SIZE, 0xffffff);

		// right line
		Rendering.drawColoredRect(x + TICKBOX_SIZE - 1, y, 1, TICKBOX_SIZE, 0xffffff);

		if (state != CheckboxState.NONE) {
			int color = state == CheckboxState.INSTALLED ? 0x7777ff : 0x00ff00;
			Rendering.drawColoredRect(x + 2, y + 2, TICKBOX_SIZE - 4, TICKBOX_SIZE - 4, color);
		}
	}

	void updateButtonState() {
		if (controller.isRefreshing()) {
			buttonInstall.enabled = buttonRefresh.enabled = buttonOptimize.enabled = false;
			return;
		}
		if (controller.isInstalling()) {
			buttonInstall.drawButton = false;
		} else {
			buttonInstall.drawButton = !controller.hasFailed();
			buttonInstall.enabled = controller.isSelectionValid();
		}
		buttonRestart.drawButton = controller.isRestartPending();
		buttonRefresh.enabled = !controller.isInstalling() && !controller.hasFailed() && !controller.isRestartPending();
		buttonOptimize.enabled = !controller.isSelectionOptimized();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int btn) {
		if (showOptimizeError) {
			if (!(showOptimizeError = btn != 0)) {
				playClickSound();
			}
			return;
		}

		if (controller.hasFailed()) {
			if (btn == 0) {
				controller.resetFailure();
				playClickSound();
			}
			return;
		}

		super.mouseClicked(mouseX, mouseY, btn);
		scroller.onMouseClick(mouseX, mouseY, btn);
		if (btn != 0) {
			return;
		}

		int modIndex = getMouseOverIndex(mouseX, mouseY);
		UpdatableMod mod;
		if (modIndex >= 0 && (mod = JavaUtils.get(mods, modIndex)) != null) {
			if (mouseX <= SCROLLER_X + MODS_START_X + fontRenderer.getStringWidth(getDisplayString(mod))) {
				markedMod = markedMod == mod ? null : mod;
				playClickSound();
			}
		}

		if (markedMod != null && !controller.isInstalling() && !controller.isRestartPending()) {
			mod = markedMod;
			int numVersions = mod.getVersions().getAvailableVersions().size();
			if (mouseX >= markedModCheckboxXStart
					&& mouseX <= markedModCheckboxXStart + TICKBOX_SIZE
					&& mouseY >= VERSIONS_Y_START
					&& mouseY <= VERSIONS_Y_START + MOD_HEIGHT * numVersions) {
				int versionIndex = MathHelper.floor_float((mouseY - VERSIONS_Y_START) / (float) MOD_HEIGHT);
				if (mod.getVersions().selectVersion(versionIndex)) {
					playClickSound();
					updateButtonState();
				}
			}
		}
	}

	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int btn) {
		scroller.onMouseBtnReleased(btn);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		scroller.onMouseWheel(Mouse.getEventDWheel());
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int btn, long time) {
		super.mouseClickMove(mouseX, mouseY, btn, time);
		scroller.onMouseMoved(mouseX, mouseY);
	}

	private int getMouseOverIndex(int mouseX, int mouseY) {
		if (mouseX >= SCROLLER_X && mouseY >= SCROLLER_Y && mouseY <= SCROLLER_Y + modListLastY) {
			return MathHelper.floor_float((mouseY - (SCROLLER_Y)) / (float) MOD_HEIGHT);
		} else {
			return -1;
		}
	}

	private void playClickSound() {
		mc.sndManager.playSoundFX("random.click", 1, 1);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case BUTTON_INSTALL:
				controller.performInstall();
				break;
			case BUTTON_OPTIMIZE:
				controller.optimizeVersionSelection();
				break;
			case BUTTON_RECHECK:
				controller.searchForUpdates();
				break;
			case BUTTON_RESTART:
				controller.restartMinecraft();
				break;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (ticks++ == 30) {
			ticks = 0;
		}
	}

	private static final String CHECKING_MARKER = "*";
	int checkingMarkerWidth;

	private class ModScroller extends ScrollPane {

		private ModScroller(int x, int y, int width, int height, int contentHeight) {
			super(GuiNewUpdates.this, x, y, width, height, contentHeight);
		}

		@Override
		protected void drawImpl(int mouseX, int mouseY) {
			int maxWidth = headerWidth;
			int y = 1;
			for (UpdatableMod mod : mods) {
				String display = getDisplayString(mod);
				int width = fontRenderer.getStringWidth(display);
				maxWidth = Math.max(maxWidth, width);
				if (mod == markedMod) {
					Rendering.drawColoredRect(MODS_START_X + checkingMarkerWidth, y - 1, width, MOD_HEIGHT, 0xffffff, 0x33);
				}

				if (mod.getState() == ModUpdateState.REFRESHING) {
					EnumChatFormatting color = ticks <= 15 ? YELLOW : DARK_AQUA;
					fontRenderer.drawString(color + CHECKING_MARKER, MODS_START_X, y, 0xffffff);
				}

				fontRenderer.drawString(display, MODS_START_X + checkingMarkerWidth, y, 0xffffff);

				boolean mouseOverY = mouseY >= y && mouseY <= y + 9;
				boolean mouseOverName = mouseOverY && mouseX >= TICKBOX_SIZE && mouseX <= width;

				if (mouseOverName) {
					Rendering.drawColoredRect(MODS_START_X + checkingMarkerWidth, y + MOD_HEIGHT - 2, width, 1, 0xffffff);
				}

				y += MOD_HEIGHT;
			}

			scrollerWidth = maxWidth + TICKBOX_SIZE + scrollbarWidth;
			setWidth(maxWidth + TICKBOX_SIZE + 3 + scrollbarWidth);
			modListLastY = y;
		}

		void updateHeight() {
			setContentHeight(MOD_HEIGHT * controller.getMods().size());
		}

		@Override
		protected void drawImpl() { }
	}

	@Override
	protected void close() {
		super.close();
		new PacketUpdaterAction(PacketUpdaterAction.Action.CLOSE).sendToServer();
	}

	private static enum CheckboxState {

		NONE,
		SELECTED,
		INSTALLED

	}
}
