package de.take_weiland.mods.commons.client;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.gui.AdvancedContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public abstract class AbstractGuiContainer<R extends IInventory, T extends Container & AdvancedContainer<R>> extends GuiContainer implements ContainerGui<T> {

	private final ResourceLocation texture;
	protected final T container;
	
	public AbstractGuiContainer(T container) {
		super(container);
		this.container = container;
		texture = provideTexture();
	}

	@Override
	public Minecraft getMinecraft() {
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
