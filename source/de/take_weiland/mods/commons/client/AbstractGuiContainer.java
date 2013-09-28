package de.take_weiland.mods.commons.client;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.templates.AdvancedContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiContainer<I extends IInventory, C extends Container & AdvancedContainer<I>> extends GuiContainer implements ContainerGui<C> {

	private final ResourceLocation texture;
	protected final String inventoryName;
	
	protected final C container;
	
	public AbstractGuiContainer(C container) {
		super(container);
		this.container = container;
		texture = provideTexture();
		
		IInventory inv = container.inventory();
		inventoryName = inv.isInvNameLocalized() ? inv.getInvName() : I18n.getString(inv.getInvName());
	}

	@Override
	public final Minecraft getMinecraft() {
		return mc;
	}

	@Override
	public final ResourceLocation getTexture() {
		return texture;
	}
	
	protected final void bindTexture() {
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
		this.fontRenderer.drawString(inventoryName, 8, 6, 0x404040);
	}

	protected final void triggerButton(int buttonId) {
		mc.playerController.sendEnchantPacket(container.windowId, UnsignedBytes.checkedCast(buttonId));
		container.clickButton(Side.CLIENT, mc.thePlayer, buttonId);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (container.handlesButton(mc.thePlayer, button.id)) {
			triggerButton(button.id);
		}
	}

}
