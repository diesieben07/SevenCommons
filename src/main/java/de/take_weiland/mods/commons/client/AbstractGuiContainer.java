package de.take_weiland.mods.commons.client;

import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.SCContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * A base implementation of GuiContainer
 * @author diesieben07
 *
 * @param <I> An IInventory
 * @param <C> An SCContainer
 */
public abstract class AbstractGuiContainer<I extends IInventory, C extends Container & SCContainer<I>> extends GuiContainer {

	protected final ResourceLocation texture;
	protected final C container;
	
	public AbstractGuiContainer(C container) {
		super(container);
		this.container = container;
		texture = provideTexture();
	}

	public final void bindTexture() {
		GL11.glColor3f(1, 1, 1);
		mc.renderEngine.bindTexture(texture);
	}
	
	protected abstract ResourceLocation provideTexture();

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (texture != null) {
			bindTexture();
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(getInvDisplayName(), 8, 6, 0x404040);
	}

	protected final String getInvDisplayName() {
		IInventory inv = container.inventory();
		return inv.isInvNameLocalized() ? inv.getInvName() : I18n.getString(inv.getInvName());
	}

	protected final void triggerButton(int buttonId) {
		mc.playerController.sendEnchantPacket(container.windowId, UnsignedBytes.checkedCast(buttonId));
		container.onButtonClick(Side.CLIENT, mc.thePlayer, buttonId);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (container.handlesButton(mc.thePlayer, button.id)) {
			triggerButton(button.id);
		}
	}

	public final C getContainer() {
		return container;
	}

}
