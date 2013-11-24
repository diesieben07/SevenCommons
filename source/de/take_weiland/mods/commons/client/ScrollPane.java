package de.take_weiland.mods.commons.client;

import static de.take_weiland.mods.commons.client.Guis.isPointInRegion;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.SCGuiScreenAccessor;
import net.minecraft.util.MathHelper;

import com.google.common.collect.Lists;

public abstract class ScrollPane extends Gui {

	private boolean clip;
	
	private int x;
	private int y;
	private int width;
	private int height;
	private int contentHeight;
	
	private int scrollbarWidth = 10;
	private int scrollbarHeight = 30;
	private int scrollbarClickOffset = 0;
	private int scrollbarY = 0;
	private boolean isDragging = false;
	
	private final GuiScreen screen;
	private final Minecraft mc = Minecraft.getMinecraft();
	private final List<GuiButton> buttons = Lists.newArrayList();
	
	public ScrollPane(GuiScreen screen, int x, int y, int width, int height, int contentHeight) {
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.contentHeight = contentHeight;
	}

	public final void draw(int mouseX, int mouseY) {
		float yTranslate = computeYTranslate();
		mouseX -= x;
		mouseY -= yTranslate;
		
		glColor3f(1, 1, 1);
		int scale = Guis.computeGuiScale(mc);
		
		if (clip) {
			glScissor(0, mc.displayHeight - (y + height) * scale, (width + x) * scale, height * scale);
			glEnable(GL_SCISSOR_TEST);
		}
		
		glPushMatrix();
		
		glTranslatef(x, yTranslate, 0);
		
		drawInternal(mouseX, mouseY);
		
		glPopMatrix();
		
		if (clip) {
			glDisable(GL_SCISSOR_TEST);
		}
		
		if (needsScrollbar()) {
			int scrollbarX = x + (width - scrollbarWidth);
			drawScrollbar(scrollbarX, y + scrollbarY, scrollbarX + scrollbarWidth, y + scrollbarY + scrollbarHeight);
		}
	}

	private float computeYTranslate() {
		return y - ((scrollbarY) / (float)(height - scrollbarHeight)) * (contentHeight - height);
	}

	private void drawInternal(int mouseX, int mouseY) {
		drawImpl();
		int n = buttons.size();
		for (int i = 0; i < n; ++i) {
			buttons.get(i).drawButton(mc, mouseX, mouseY);
		}
	}
	
	private boolean needsScrollbar() {
		return height < contentHeight;
	}
	
	public final void onMouseClick(int mouseX, int mouseY, int btn) {
		if (btn == 0 && needsScrollbar()) {
			if (Guis.isPointInRegion(x + (width - scrollbarWidth), y + scrollbarY, scrollbarWidth, scrollbarHeight, mouseX, mouseY)) {
				isDragging = true;
				scrollbarClickOffset = mouseY - (y + scrollbarY);
			}
		}
		
		mouseX -= x;
		mouseY -= computeYTranslate();
		if (isPointInRegion(0, 0, width, height, mouseX, mouseY)) {
			for (GuiButton button : buttons) {
				if (button.mousePressed(mc, mouseX, mouseY)) {
					SCGuiScreenAccessor.actionPerfomed(screen, button);
				}
			}
		}
		handleMouseClick(mouseX, mouseY, btn);
	}
	
	public final void onMouseBtnReleased(int btn) {
		if (btn == 0) {
			isDragging = false;
		}
	}
	
	public final void onMouseMoved(int mouseX, int mouseY) {
		if (isDragging) {
			scrollbarY = MathHelper.clamp_int(mouseY - y - scrollbarClickOffset, 0, height - scrollbarHeight);
		}
	}

	public final void setScrollbarWidth(int scrollbarWidth) {
		this.scrollbarWidth = scrollbarWidth;
	}
	
	public final void setX(int x) {
		this.x = x;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final void setWidth(int width) {
		this.width = width;
	}

	public final void setHeight(int height) {
		this.height = height;
	}
	
	public final void setContentHeight(int contentHeight) {
		this.contentHeight = contentHeight;
	}

	public final void clearButtons() {
		buttons.clear();
	}
	
	public final void addButton(GuiButton button) {
		buttons.add(button);
	}
	
	public final void setClip(boolean clip) {
		this.clip = clip;
	}

	protected void drawScrollbar(int x, int y, int x2, int y2) {
		drawRect(x, y, x2, y2, 0xff000000);
		drawRect(x, y, x + 1, y2, 0xff444444);
		drawRect(x2 - 1, y, x2, y2, 0xff444444);
		drawRect(x, y, x2, y + 1, 0xff444444);
		drawRect(x, y2 - 1, x2, y2, 0xff444444);
	}
	
	protected abstract void drawImpl();
	
	protected void handleMouseClick(int relX, int relY, int btn) {
	}

}
