package de.take_weiland.mods.commons.client;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

import static java.lang.invoke.MethodHandles.publicLookup;
import static net.minecraft.client.Minecraft.getMinecraft;
import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * <p>Helpful utilities for various rendering tasks.</p>
 *
 * @author diesieben07
 */
public final class Rendering {

    private static final MethodHandle timerGet;

    static {
        try {
            Field field = Minecraft.class.getDeclaredField(MCPNames.field(SRGConstants.F_MINECRAFT_TIMER));
            field.setAccessible(true);
            timerGet = MethodHandles.publicLookup().unreflectGetter(field);
        } catch (Exception x) {
            throw Throwables.propagate(x);
        }
    }

    /**
     * <p>Get the current render partial tick value.</p>
     *
     * @return the render partial tick value
     */
    public static float getPartialTicks() {
        try {
            return ((Timer) timerGet.invokeExact(getMinecraft())).renderPartialTicks;
        } catch (Throwable x) {
            throw JavaUtils.throwUnchecked(x);
        }
    }

    /**
     * <p>Fills a specified area on the screen with the provided {@link TextureAtlasSprite}.</p>
     *
     * @param icon   The {@link TextureAtlasSprite} to be displayed
     * @param x      The X coordinate to start drawing from
     * @param y      The Y coordinate to start drawing form
     * @param width  The width of the provided icon to draw on the screen
     * @param height The height of the provided icon to draw on the screen
     */
    public static void fillAreaWithIcon(TextureAtlasSprite icon, int x, int y, int width, int height) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        b.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float zLevel = getZLevel();

        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();

        // number of rows & cols of full size icons
        int fullCols = width / iconWidth;
        int fullRows = height / iconHeight;

        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        int excessWidth = width % iconWidth;
        int excessHeight = height % iconHeight;

        // interpolated max u/v for the excess row / col
        float partialMaxU = minU + (maxU - minU) * ((float) excessWidth / iconWidth);
        float partialMaxV = minV + (maxV - minV) * ((float) excessHeight / iconHeight);

        int xNow;
        int yNow;
        for (int row = 0; row < fullRows; row++) {
            yNow = y + row * iconHeight;
            for (int col = 0; col < fullCols; col++) {
                // main part, only full icons
                xNow = x + col * iconWidth;
                drawRect(xNow, yNow, iconWidth, iconHeight, zLevel, minU, minV, maxU, maxV);
            }
            if (excessWidth != 0) {
                // last not full width column in every row at the end
                xNow = x + fullCols * iconWidth;
                drawRect(xNow, yNow, iconWidth, iconHeight, zLevel, minU, minV, maxU, maxV);
            }
        }
        if (excessHeight != 0) {
            // last not full height row
            for (int col = 0; col < fullCols; col++) {
                xNow = x + col * iconWidth;
                yNow = y + fullRows * iconHeight;
                drawRect(xNow, yNow, iconWidth, excessHeight, zLevel, minU, minV, maxU, partialMaxV);
            }
            if (excessWidth != 0) {
                // missing quad in the bottom right corner of neither full height nor full width
                xNow = x + fullCols * iconWidth;
                yNow = y + fullRows * iconHeight;
                drawRect(xNow, yNow, excessWidth, excessHeight, zLevel, minU, minV, partialMaxU, partialMaxV);
            }
        }

        t.draw();
    }

    private static void drawRect(float x, float y, float width, float height, float z, float u, float v, float maxU, float maxV) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.pos(x, y + height, z).tex(u, maxV).endVertex();
        b.pos(x + width, y + height, z).tex(maxU, maxV).endVertex();
        b.pos(x + width, y, z).tex(maxU, v).endVertex();
        b.pos(x, y, z).tex(u, v).endVertex();
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

        if (fluidStack != null) {
            Fluid fluid = fluidStack.getFluid();
            TextureAtlasSprite fluidIcon = getMinecraft().getTextureMapBlocks().getTextureExtry(fluid.getStill(fluidStack).toString());
            int fluidHeight = MathHelper.ceil((fluidStack.amount / (double) tankCapacity) * fullHeight);

            color(1, 1, 1);
            getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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
        TextureManager renderEngine = getMinecraft().renderEngine;

        if (fluidStack != null) {
            Fluid fluid = fluidStack.getFluid();
            TextureAtlasSprite fluidIcon = getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill(fluidStack).toString());
            int fluidWidth = MathHelper.ceil((fluidStack.amount / (float) tankCapacity) * fullWidth);

            color(1, 1, 1);
            renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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

        float cr = (float) (color >> 16 & 0xFF) / 255.0F;
        float cg = (float) (color >> 8 & 0xFF) / 255.0F;
        float cb = (float) (color & 0xFF) / 255.0F;

        disableTexture2D();

        if (alpha != 255) {
            enableBlend();
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        color(cr, cg, cb, alpha);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();

        b.begin(GL_QUADS, DefaultVertexFormats.POSITION);

        b.pos(x, y + height, zLevel).endVertex();
        b.pos(x + width, y + height, zLevel).endVertex();
        b.pos(x + width, y, zLevel).endVertex();
        b.pos(x, y, zLevel).endVertex();

        t.draw();

        enableTexture2D();
        if (alpha != 255) {
            disableBlend();
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
            enableBlend();
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        disableTexture2D();
        disableAlpha();
        shadeModel(GL_SMOOTH);

        Tessellator t = Tessellator.getInstance();

        t.getBuffer().begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        if (horizontal) {
            horizontalGradient0(x, y, width, height, fromAlpha, toAlpha, zLevel, r1, g1, b1, r2, g2, b2);
        } else {
            verticalGradient0(x, y, width, height, fromAlpha, toAlpha, zLevel, r1, g1, b1, r2, g2, b2);
        }

        t.draw();

        shadeModel(GL_FLAT);
        enableTexture2D();
        enableAlpha();

        if (needBlending) {
            disableBlend();
        }
    }

    private static void verticalGradient0(int x, int y, int width, int height, int fromAlpha, int toAlpha, float zLevel, float r1, float g1, float b1, float r2, float g2, float b2) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.color(r1, g1, b1, fromAlpha / 255f).pos(x, y + height, zLevel).endVertex();
        b.color(r1, g1, b1, fromAlpha / 255f).pos(x + width, y + height, zLevel).endVertex();
        b.color(r2, g2, b2, toAlpha / 255f).pos(x + width, y, zLevel).endVertex();
        b.color(r2, g2, b2, toAlpha / 255f).pos(x, y, zLevel).endVertex();
    }

    private static void horizontalGradient0(int x, int y, int width, int height, int fromAlpha, int toAlpha, float zLevel, float r1, float g1, float b1, float r2, float g2, float b2) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();

        b.color(r1, g1, b1, fromAlpha / 255f).pos(x, y + height, zLevel).endVertex();
        b.color(r2, g2, b2, toAlpha / 255F).pos(x + width, y + height, zLevel).endVertex();
        b.color(r2, g2, b2, toAlpha / 255F).pos(x + width, y, zLevel).endVertex();
        b.color(r1, g1, b1, fromAlpha / 255F).pos(x, y, zLevel).endVertex();
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

        drawTexturedQuad(x, y, width, height, u * uFact, v * vFact, uEnd, vEnd, zLevel);
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
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();

        b.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        b.pos(x, y + height, zLevel).tex(uStart, vEnd).endVertex();
        b.pos(x + width, y + height, zLevel).tex(uEnd, vEnd).endVertex();
        b.pos(x + width, y, zLevel).tex(uEnd, vStart).endVertex();
        b.pos(x, y, zLevel).tex(uStart, vStart).endVertex();

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
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();

        b.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        b.pos(x, y + height, zLevel).tex(0, 1).endVertex();
        b.pos(x + width, y + height, zLevel).tex(1, 1).endVertex();
        b.pos(x + width, y, zLevel).tex(1, 0).endVertex();
        b.pos(x, y, zLevel).tex(0, 0).endVertex();

        t.draw();
    }

    /**
     * <p>Unload the texture specified by the given ResourceLocation.</p>
     *
     * @param loc the ResourceLocation
     */
    public static void unloadTexture(ResourceLocation loc) {
        ITextureObject tex;
        try {
            //noinspection unchecked
            tex = ((Map<ResourceLocation, ITextureObject>) texturesMapGet.invokeExact(getMinecraft().renderEngine)).remove(loc);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }

        if (tex != null) {
            glDeleteTextures(tex.getGlTextureId());
        }
    }

    private static float getZLevel() {
        GuiScreen screen = getMinecraft().currentScreen;
        try {
            return screen == null ? 0 : (float) zLevelGet.invokeExact((Gui) screen);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    private static final MethodHandle texturesMapGet;
    private static final MethodHandle zLevelGet;

    static {
        try {
            Field field = TextureManager.class.getDeclaredField(MCPNames.field(SRGConstants.F_MAP_TEXTURE_OBJECTS));
            field.setAccessible(true);
            texturesMapGet = publicLookup().unreflectGetter(field);

            field = Gui.class.getDeclaredField(MCPNames.field(SRGConstants.F_Z_LEVEL));
            field.setAccessible(true);
            zLevelGet = publicLookup().unreflectGetter(field);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    private Rendering() {
    }

}
