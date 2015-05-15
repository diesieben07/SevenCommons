package de.take_weiland.mods.commons.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * <p>A base implementation for GuiContainer.</p>
 *
 * @author diesieben07
 */
public abstract class AbstractGuiContainer<C extends Container> extends GuiContainer {

    protected final ResourceLocation texture;
    protected final C container;

    /**
     * <p>Create a new AbstractGuiContainer.</p>
     *
     * @param container the Container
     */
    public AbstractGuiContainer(C container) {
        super(container);
        this.container = container;
        this.texture = provideTexture();
    }

    /**
     * <p>Bind the background texture for this Gui.</p>
     */
    public final void bindTexture() {
        GL11.glColor3f(1, 1, 1);
        mc.renderEngine.bindTexture(texture);
    }

    /**
     * <p>Create the ResourceLocation for the background texture of this Gui.</p>
     *
     * @return a ResourceLocation
     */
    protected abstract ResourceLocation provideTexture();

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (texture != null) {
            bindTexture();
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }
    }

    /**
     * <p>Get the Container bound to this Gui.</p>
     *
     * @return the Container
     */
    public final C getContainer() {
        return container;
    }

}
