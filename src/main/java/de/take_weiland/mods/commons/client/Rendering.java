package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.internal.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;

import static net.minecraftforge.common.util.ForgeDirection.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * <p>Helpful utilities for various rendering tasks.</p>
 *
 * @author diesieben07
 */
public final class Rendering {

    /**
     * <p>Fills a specified area on the screen with the provided {@link net.minecraft.util.IIcon Icon}.</p>
     *
     * @param icon   The {@link net.minecraft.util.IIcon Icon} to be displayed
     * @param x      The X coordinate to start drawing from
     * @param y      The Y coordinate to start drawing form
     * @param width  The width of the provided icon to draw on the screen
     * @param height The height of the provided icon to draw on the screen
     */
    public static void fillAreaWithIcon(IIcon icon, int x, int y, int width, int height) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        float zLevel = getZLevel();

        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();

        // number of rows & cols of "full" icons (full size)
        int fullCols = MathHelper.floor_float(width / (float) iconWidth);
        int fullRows = MathHelper.floor_float(height / (float) iconHeight);

        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        // interpolated max u/v for the excess row / col
        float partialMaxU = minU + (maxU - minU) * ((width - fullCols * iconWidth) / (float) width);
        float partialMaxV = minV + (maxV - minV) * ((height - fullRows * iconHeight) / (float) height);

        boolean excessCol = (width % iconWidth) != 0;
        boolean excessRow = (height % iconHeight) != 0;

        int xNow;
        int yNow;
        for (int row = 0; row < fullRows; row++) {
            yNow = y + row * iconHeight;
            for (int col = 0; col < fullCols; col++) {
                // main part, only full icons
                xNow = x + col * iconWidth;
                t.addVertexWithUV(xNow, yNow + iconHeight, zLevel, minU, maxV);
                t.addVertexWithUV(xNow + iconWidth, yNow + iconHeight, zLevel, maxU, maxV);
                t.addVertexWithUV(xNow + iconWidth, yNow, zLevel, maxU, minV);
                t.addVertexWithUV(xNow, yNow, zLevel, minU, minV);
            }
            if (excessCol) {
                // excess part in every row at the end of the columns
                xNow = x + fullCols * iconWidth;
                t.addVertexWithUV(xNow, yNow + iconHeight, zLevel, minU, maxV);
                t.addVertexWithUV(x + width, yNow + iconHeight, zLevel, partialMaxU, maxV);
                t.addVertexWithUV(x + width, yNow, zLevel, partialMaxU, minV);
                t.addVertexWithUV(xNow, yNow, zLevel, minU, minV);
            }
        }
        if (excessRow) {
            // last "excess" row
            for (int col = 0; col < fullCols; col++) {
                xNow = x + col * iconWidth;
                yNow = y + fullRows * iconHeight;
                t.addVertexWithUV(xNow, y + height, zLevel, minU, partialMaxV);
                t.addVertexWithUV(xNow + iconWidth, y + height, zLevel, maxU, partialMaxV);
                t.addVertexWithUV(xNow + iconWidth, yNow, zLevel, maxU, minV);
                t.addVertexWithUV(xNow, yNow, zLevel, minU, minV);
            }
            if (excessCol) {
                // missing quad in the bottom right corner when excess row & col
                xNow = x + fullCols * iconWidth;
                yNow = y + fullRows * iconHeight;
                t.addVertexWithUV(xNow, y + height, zLevel, minU, partialMaxV);
                t.addVertexWithUV(x + width, y + height, zLevel, partialMaxU, partialMaxV);
                t.addVertexWithUV(x + width, yNow, zLevel, partialMaxU, minV);
                t.addVertexWithUV(xNow, yNow, zLevel, minU, minV);
            }
        }

        t.draw();
    }

    /**
     * <p>Draw a vertical bar representing the fullness of a Tank filled with the given FluidStack and of the given capacity.</p>
     *
     * @param fluidStack   the FluidStack
     * @param tankCapacity the capacity of the tank (maximum amount of fluid in the FluidStack)
     * @param x            the x coordinate
     * @param y            the y coordinate
     * @param width        the width of the rectangle to draw
     * @param fullHeight   the height of the rectangle when the tank is full
     */
    public static void drawFluidStack(@Nullable FluidStack fluidStack, int tankCapacity, int x, int y, int width, int fullHeight) {
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

        if (fluidStack != null) {
            Fluid fluid = fluidStack.getFluid();
            IIcon fluidIcon = fluid.getStillIcon();
            int fluidHeight = MathHelper.ceiling_float_int((fluidStack.amount / (float) tankCapacity) * fullHeight);

            glColor3f(1, 1, 1);
            renderEngine.bindTexture(renderEngine.getResourceLocation(fluid.getSpriteNumber()));
            fillAreaWithIcon(fluidIcon, x, y + fullHeight - fluidHeight, width, fluidHeight);
        }
    }

    /**
     * <p>Draw a horizontal bar representing the fullness of a Tank filled with the given FluidStack and of the given capacity.</p>
     *
     * @param fluidStack   the FluidStack
     * @param tankCapacity the capacity of the tank (maximum amount of fluid in the FluidStack)
     * @param x            the x coordinate
     * @param y            the y coordinate
     * @param fullWidth    the width of the rectangle when the tank is full
     * @param height       the height of the rectangle
     */
    public static void drawFluidStackHorizontal(@Nullable FluidStack fluidStack, int tankCapacity, int x, int y, int fullWidth, int height) {
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

        if (fluidStack != null) {
            Fluid fluid = fluidStack.getFluid();
            IIcon fluidIcon = fluid.getStillIcon();
            int fluidWidth = MathHelper.ceiling_float_int((fluidStack.amount / (float) tankCapacity) * fullWidth);

            glColor3f(1, 1, 1);
            renderEngine.bindTexture(renderEngine.getResourceLocation(fluid.getSpriteNumber()));
            fillAreaWithIcon(fluidIcon, x, y, fluidWidth, height);
        }
    }

    /**
     * <p>Draw a vertical bar representing the fullness of the given {@code IFluidTank}.</p>
     *
     * @param tank       the tank
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param width      the width of the rectangle to draw
     * @param fullHeight the height of the rectangle, if the tank is full
     */
    public static void drawTank(IFluidTank tank, int x, int y, int width, int fullHeight) {
        drawFluidStack(tank.getFluid(), tank.getCapacity(), x, y, width, fullHeight);
    }

    /**
     * <p>Draw a horizontal bar representing the fullness of the given {@code IFluidTank}.</p>
     *
     * @param tank      the tank
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param fullWidth the width of the rectangle when the tank is full
     * @param height    the height of the rectangle
     */
    public static void drawTankHorizontal(IFluidTank tank, int x, int y, int fullWidth, int height) {
        drawFluidStackHorizontal(tank.getFluid(), tank.getCapacity(), x, y, fullWidth, height);
    }

    /**
     * <p>Draw a simple inventory block. This method draws a cuboid based on the current block dimensions.</p>
     *
     * @param block    the Block to draw
     * @param meta     the Block metadata
     * @param renderer the RenderBlocks instance
     */
    public static void drawInventoryBlock(Block block, int meta, RenderBlocks renderer) {
        Tessellator t = Tessellator.instance;

        t.startDrawingQuads();
        t.setNormal(-1, 0, 0);
        renderer.renderFaceXNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, WEST.ordinal(), meta));
        t.draw();

        t.startDrawingQuads();
        t.setNormal(1, 0, 0);
        renderer.renderFaceXPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, EAST.ordinal(), meta));
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 0, -1);
        renderer.renderFaceZNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, NORTH.ordinal(), meta));
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 0, 1);
        renderer.renderFaceZPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, SOUTH.ordinal(), meta));
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, -1, 0);
        renderer.renderFaceYNeg(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, DOWN.ordinal(), meta));
        t.draw();

        t.startDrawingQuads();
        t.setNormal(0, 1, 0);
        renderer.renderFaceYPos(block, 0, 0, 0, renderer.getBlockIconFromSideAndMetadata(block, UP.ordinal(), meta));
        t.draw();
    }

    /**
     * <p>Draw a colored rectangle.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param color  the color (in RGB format, e.g. {@code 0xFF0000})
     */
    public static void drawColoredQuad(int x, int y, int width, int height, int color) {
        drawColoredQuad(x, y, width, height, color, 0xFF, getZLevel());
    }

    /**
     * <p>Draw a colored rectangle.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param color  the color (in RGB format, e.g. {@code 0xFF0000})
     * @param alpha  the opacity of the rectangle (0 = completely transparent, 255 = completely opaque)
     */
    public static void drawColoredQuad(int x, int y, int width, int height, int color, int alpha) {
        drawColoredQuad(x, y, width, height, color, alpha, getZLevel());
    }

    /**
     * <p>Draw a colored rectangle.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param color  the color (in RGB format, e.g. {@code 0xFF0000})
     * @param alpha  the opacity of the rectangle (0 = completely transparent, 255 = completely opaque)
     * @param zLevel the z-level to draw at
     */
    public static void drawColoredQuad(int x, int y, int width, int height, int color, int alpha, float zLevel) {
        if (alpha == 0) {
            return;
        }

        float r = (float) (color >> 16 & 0xFF) / 255.0F;
        float g = (float) (color >> 8 & 0xFF) / 255.0F;
        float b = (float) (color & 0xFF) / 255.0F;

        glDisable(GL_TEXTURE_2D);

        if (alpha != 255) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glColor4f(r, g, b, alpha / 255F);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        t.addVertex(x, y + height, zLevel);
        t.addVertex(x + width, y + height, zLevel);
        t.addVertex(x + width, y, zLevel);
        t.addVertex(x, y, zLevel);

        t.draw();

        glEnable(GL_TEXTURE_2D);
        if (alpha != 255) {
            glDisable(GL_BLEND);
        }
    }

    /**
     * <p>Draw a vertical gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param toColor   the end color in RGB format
     */
    public static void verticalGradient(int x, int y, int width, int height, int fromColor, int toColor) {
        drawGradient(false, x, y, width, height, fromColor, toColor);
    }

    /**
     * <p>Draw a vertical gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param fromAlpha the alpha value for the start color (0-255)
     * @param toColor   the end color in RGB format
     * @param toAlpha   the alpha value for the end color (0-255)
     */
    public static void verticalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
        drawGradient(false, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha);
    }

    /**
     * <p>Draw a vertical gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param fromAlpha the alpha value for the start color (0-255)
     * @param toColor   the end color in RGB format
     * @param toAlpha   the alpha value for the end color (0-255)
     * @param zLevel    the z-level to draw at
     */
    public static void verticalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
        drawGradient(false, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, zLevel);
    }

    /**
     * <p>Draw a horizontal gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param toColor   the end color in RGB format
     */
    public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int toColor) {
        drawGradient(true, x, y, width, height, fromColor, toColor);
    }

    /**
     * <p>Draw a horizontal gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param fromAlpha the alpha value for the start color (0-255)
     * @param toColor   the end color in RGB format
     * @param toAlpha   the alpha value for the end color (0-255)
     */
    public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
        drawGradient(true, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha);
    }

    /**
     * <p>Draw a horizontal gradient.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the gradient
     * @param height    the height of the gradient
     * @param fromColor the start color in RGB format
     * @param fromAlpha the alpha value for the start color (0-255)
     * @param toColor   the end color in RGB format
     * @param toAlpha   the alpha value for the end color (0-255)
     * @param zLevel    the z-level to draw at
     */
    public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
        drawGradient(true, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, zLevel);
    }

    /**
     * <p>Draw a gradient.</p>
     *
     * @param horizontal if a horizontal or vertical gradient should be drawn
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param width      the width of the gradient
     * @param height     the height of the gradient
     * @param fromColor  the start color in RGB format
     * @param toColor    the end color in RGB format
     */
    public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int toColor) {
        drawGradient(horizontal, x, y, width, height, fromColor, 0xFF, toColor, 0xFF, getZLevel());
    }

    /**
     * <p>Draw a gradient.</p>
     *
     * @param horizontal if a horizontal or vertical gradient should be drawn
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param width      the width of the gradient
     * @param height     the height of the gradient
     * @param fromColor  the start color in RGB format
     * @param fromAlpha  the alpha value for the start color (0-255)
     * @param toColor    the end color in RGB format
     * @param toAlpha    the alpha value for the end color (0-255)
     */
    public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
        drawGradient(horizontal, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, getZLevel());
    }

    /**
     * <p>Draw a gradient.</p>
     *
     * @param horizontal if a horizontal or vertical gradient should be drawn
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param width      the width of the gradient
     * @param height     the height of the gradient
     * @param fromColor  the start color in RGB format
     * @param fromAlpha  the alpha value for the start color (0-255)
     * @param toColor    the end color in RGB format
     * @param toAlpha    the alpha value for the end color (0-255)
     * @param zLevel     the z-level to draw at
     */
    public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
        if (fromAlpha == 0 && toAlpha == 0) {
            return;
        }

        boolean needBlending = fromAlpha != 255 || toAlpha != 255;

        float r1 = (float) (fromColor >> 16 & 0xFF) / 255.0F;
        float g1 = (float) (fromColor >> 8 & 0xFF) / 255.0F;
        float b1 = (float) (fromColor & 0xFF) / 255.0F;

        float r2 = (float) (toColor >> 16 & 0xFF) / 255.0F;
        float g2 = (float) (toColor >> 8 & 0xFF) / 255.0F;
        float b2 = (float) (toColor & 0xFF) / 255.0F;

        if (needBlending) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_ALPHA_TEST);
        glShadeModel(GL_SMOOTH);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();

        if (horizontal) {
            horizontalGradient0(x, y, width, height, fromAlpha, toAlpha, zLevel, r1, g1, b1, r2, g2, b2);
        } else {
            verticalGradient0(x, y, width, height, fromAlpha, toAlpha, zLevel, r1, g1, b1, r2, g2, b2);
        }

        t.draw();

        glShadeModel(GL_FLAT);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);

        if (needBlending) {
            glDisable(GL_BLEND);
        }
    }

    private static void verticalGradient0(int x, int y, int width, int height, int fromAlpha, int toAlpha, float zLevel, float r1, float g1, float b1, float r2, float g2, float b2) {
        Tessellator t = Tessellator.instance;
        t.setColorRGBA_F(r1, g1, b1, fromAlpha / 255F);
        t.addVertex(x, y + height, zLevel);
        t.addVertex(x + width, y + height, zLevel);

        t.setColorRGBA_F(r2, g2, b2, toAlpha / 255F);
        t.addVertex(x + width, y, zLevel);
        t.addVertex(x, y, zLevel);
    }

    private static void horizontalGradient0(int x, int y, int width, int height, int fromAlpha, int toAlpha, float zLevel, float r1, float g1, float b1, float r2, float g2, float b2) {
        Tessellator t = Tessellator.instance;
        t.setColorRGBA_F(r1, g1, b1, fromAlpha / 255F);
        t.addVertex(x, y + height, zLevel);

        t.setColorRGBA_F(r2, g2, b2, toAlpha / 255F);
        t.addVertex(x + width, y + height, zLevel);

        t.setColorRGBA_F(r2, g2, b2, toAlpha / 255F);
        t.addVertex(x + width, y, zLevel);

        t.setColorRGBA_F(r1, g1, b1, fromAlpha / 255F);
        t.addVertex(x, y, zLevel);
    }

    /**
     * <p>Draw a textured rectangle.</p>
     *
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param width   the width of the rectangle
     * @param height  the height of the rectangle
     * @param u       the x coordinate in the texture
     * @param v       the y coordinate in the texture
     * @param uSize   the width of the rectangle in the texture
     * @param vSize   the height of the rectangle in the texture
     * @param texSize the dimensions of the texture (width & height)
     * @param zLevel  the z-level to draw at
     */
    public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSize, float zLevel) {
        drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texSize, texSize, zLevel);
    }

    /**
     * <p>Draw a textured rectangle.</p>
     *
     * @param x       the x coordinate
     * @param y       the y coordinate
     * @param width   the width of the rectangle
     * @param height  the height of the rectangle
     * @param u       the x coordinate in the texture
     * @param v       the y coordinate in the texture
     * @param uSize   the width of the rectangle in the texture
     * @param vSize   the height of the rectangle in the texture
     * @param texSize the dimensions of the texture (width & height)
     */
    public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSize) {
        drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texSize, texSize, getZLevel());
    }

    /**
     * <p>Draw a textured rectangle.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the rectangle
     * @param height    the height of the rectangle
     * @param u         the x coordinate in the texture
     * @param v         the y coordinate in the texture
     * @param uSize     the width of the rectangle in the texture
     * @param vSize     the height of the rectangle in the texture
     * @param texWidth  the width of the texture
     * @param texHeight the height of the texture
     */
    public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texWidth, int texHeight) {
        drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texWidth, texHeight, getZLevel());
    }

    /**
     * <p>Draw a textured rectangle.</p>
     *
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param width     the width of the rectangle
     * @param height    the height of the rectangle
     * @param u         the x coordinate in the texture
     * @param v         the y coordinate in the texture
     * @param uSize     the width of the rectangle in the texture
     * @param vSize     the height of the rectangle in the texture
     * @param texWidth  the width of the texture
     * @param texHeight the height of the texture
     * @param zLevel    the z-level to draw at
     */
    public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texWidth, int texHeight, float zLevel) {
        float uFact = 1f / texWidth;
        float vFact = 1f / texHeight;

        float uEnd = (u + uSize) * uFact;
        float vEnd = (v + vSize) * vFact;

        drawTexturedQuad(x, y, width, height, u, v, uEnd, vEnd, zLevel);
    }

    /**
     * <p>Draw a textured rectangle.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param uStart the starting x coordinate in the texture
     * @param vStart the starting y coordinate in the texture
     * @param uEnd   the ending x coordinate in the texture
     * @param vEnd   the ending Y coordinate in the texture
     * @param zLevel the z-level to draw at
     */
    public static void drawTexturedQuad(int x, int y, int width, int height, float uStart, float vStart, float uEnd, float vEnd, float zLevel) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, zLevel, uStart, vEnd);
        t.addVertexWithUV(x + width, y + height, zLevel, uEnd, vEnd);
        t.addVertexWithUV(x + width, y, zLevel, uEnd, vStart);
        t.addVertexWithUV(x, y, zLevel, uStart, vStart);
        t.draw();
    }

    /**
     * <p>Draw a textured rectangle with the entire texture scaled to the given dimensions.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     */
    public static void drawTexturedQuadFit(int x, int y, int width, int height) {
        drawTexturedQuadFit(x, y, width, height, getZLevel());
    }

    /**
     * <p>Draw a textured rectangle with the entire texture scaled to the given dimensions.</p>
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param zLevel the z-level to draw at
     */
    public static void drawTexturedQuadFit(int x, int y, int width, int height, float zLevel) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, zLevel, 0, 1);
        t.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
        t.addVertexWithUV(x + width, y, zLevel, 1, 0);
        t.addVertexWithUV(x, y, zLevel, 0, 0);
        t.draw();
    }

    /**
     * <p>Unload the texture specified by the given ResourceLocation.</p>
     *
     * @param loc the ResourceLocation
     */
    public static void unloadTexture(ResourceLocation loc) {
        ITextureObject tex = SCReflector.instance.getTexturesMap(Minecraft.getMinecraft().renderEngine).remove(loc);
        if (tex != null) {
            glDeleteTextures(tex.getGlTextureId());
        }
    }

    private static float getZLevel() {
        return SCReflector.instance.getZLevel(Minecraft.getMinecraft().currentScreen);
    }

    private Rendering() {
    }

}
