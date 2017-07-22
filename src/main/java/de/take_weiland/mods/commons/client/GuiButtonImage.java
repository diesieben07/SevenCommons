package de.take_weiland.mods.commons.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;

/**
 * <p>A simple {@code GuiButton} which displays a custom image.</p>
 *
 * @author diesieben07
 */
public class GuiButtonImage extends GuiButton {

    private final ResourceLocation texture;
    private final float uStart;
    private final float vStart;
    private final float uEnd;
    private final float vEnd;

    /**
     * <p>Create a new {@code GuiButtonImage}.</p>
     * <p>There need to be 3 images provided in the given texture, vertically directly below each other and of the same
     * width and height as this button. In order: disabled, enabled, hovered.</p>
     *
     * @param buttonId the button ID
     * @param x        the x coordinate for the button
     * @param y        the y coordinate for the button
     * @param width    the width of the button
     * @param height   the height of the button
     * @param texture  the {@code ResourceLocation} specifying the texture
     * @param u        the x coordinate of the texture
     * @param v        the y coordinate of the texture
     */
    public GuiButtonImage(int buttonId, int x, int y, int width, int height, ResourceLocation texture, int u, int v) {
        super(buttonId, x, y, width, height, "");
        this.texture = texture;
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        int texWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int texHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

        float uFact = 1f / texWidth;
        float vFact = 1f / texHeight;

        uStart = u * uFact;
        vStart = v * vFact;
        uEnd = (u + width) * uFact;
        vEnd = (v + height) * vFact;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(texture);
            glColor3f(1, 1, 1);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            int hoverState = getHoverState(hovered);

            glEnable(GL_BLEND);
            OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // why?

            float vOff = (vEnd - vStart) * hoverState;
            Rendering.drawTexturedQuad(x, y, width, height, uStart, vStart + vOff, uEnd, vEnd + vOff, zLevel);

            mouseDragged(mc, mouseX, mouseY);
        }
    }
}
