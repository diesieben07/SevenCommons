package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.SCGuiAccessor;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureObject;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import static net.minecraftforge.common.ForgeDirection.*;
import static org.lwjgl.opengl.GL11.*;

public final class Rendering {
	
	private Rendering() { }
	
	private static final Gui gui = new Gui();
	
	public static void fillAreaWithIcon(Icon icon, int x, int y, int width, int height) {
		int scale = Guis.computeGuiScale();

		glScissor(0, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width + x) * scale, height * scale);
		glEnable(GL_SCISSOR_TEST);
		
		int cols = MathHelper.ceiling_float_int(width / 16F);
		int rows = MathHelper.ceiling_float_int(height / 16F);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				gui.drawTexturedModelRectFromIcon(x + col * 16, y + row * 16, icon, 16, 16);
			}
		}
		
		glDisable(GL_SCISSOR_TEST);
	}
	
	public static void drawFluidStack(IFluidTank tank, int x, int y, int width, int fullHeight) {
		TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
		
		FluidStack fluidStack = tank.getFluid();

		if (fluidStack != null) {
			Fluid fluid = fluidStack.getFluid();
			Icon fluidIcon = fluid.getStillIcon();
			int fluidHeight = MathHelper.ceiling_float_int((fluidStack.amount / (float)tank.getCapacity()) * fullHeight);

			glColor3f(1, 1, 1);
			renderEngine.bindTexture(renderEngine.getResourceLocation(fluid.getSpriteNumber()));
			fillAreaWithIcon(fluidIcon, x, y + fullHeight - fluidHeight, width, fluidHeight);
		}
	}

	public static void drawInventoryBlock(Block block, RenderBlocks renderer, int meta) {
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

	public static void drawColoredRect(int x, int y, int width, int height, int color) {
		drawColoredRect(x, y, width, height, color, 0xFF, getZLevel());
	}

	public static void drawColoredRect(int x, int y, int width, int height, int color, int alpha) {
		drawColoredRect(x, y, width, height, color, alpha, getZLevel());
	}
	
	public static void drawColoredRect(int x, int y, int width, int height, int color, int alpha, float zLevel) {
		float r = (float) (color >> 16 & 0xFF) / 255.0F;
		float g = (float) (color >> 8 & 0xFF) / 255.0F;
		float b = (float) (color & 0xFF) / 255.0F;
		
		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glColor4f(r, g, b, alpha / 255F);
		
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		
		t.addVertex(x, y + height, zLevel);
		t.addVertex(x + width, y + height, zLevel);
		t.addVertex(x + width, y, zLevel);
		t.addVertex(x, y, zLevel);
		
		t.draw();
		
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
	}

	public static void verticalGradient(int x, int y, int width, int height, int fromColor, int toColor) {
		drawGradient(false, x, y, width, height, fromColor, toColor);
	}

	public static void verticalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
		drawGradient(false, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha);
	}

	public static void verticalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
		drawGradient(false, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, zLevel);
	}

	public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int toColor) {
		drawGradient(true, x, y, width, height, fromColor, toColor);
	}

	public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
		drawGradient(true, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha);
	}

	public static void horizontalGradient(int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
		drawGradient(true, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, zLevel);
	}

	public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int toColor) {
		drawGradient(horizontal, x, y, width, height, fromColor, 0xFF, toColor, 0xFF, getZLevel());
	}

	public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha) {
		drawGradient(horizontal, x, y, width, height, fromColor, fromAlpha, toColor, toAlpha, getZLevel());
	}

	public static void drawGradient(boolean horizontal, int x, int y, int width, int height, int fromColor, int fromAlpha, int toColor, int toAlpha, float zLevel) {
		float r1 = (float) (fromColor >> 16 & 0xFF) / 255.0F;
		float g1 = (float) (fromColor >> 8 & 0xFF) / 255.0F;
		float b1 = (float) (fromColor & 0xFF) / 255.0F;

		float r2 = (float) (toColor >> 16 & 0xFF) / 255.0F;
		float g2 = (float) (toColor >> 8 & 0xFF) / 255.0F;
		float b2 = (float) (toColor & 0xFF) / 255.0F;

		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_ALPHA_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
		glDisable(GL_BLEND);
		glEnable(GL_ALPHA_TEST);
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


	public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSize, float zLevel) {
		drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texSize, texSize, zLevel);
	}
	
	public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSize) {
		drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texSize, texSize, getZLevel());
	}
	
	public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSizeX, int texSizeY) {
		drawTexturedQuad(x, y, width, height, u, v, uSize, vSize, texSizeX, texSizeY, getZLevel());
	}
	
	public static void drawTexturedQuad(int x, int y, int width, int height, int u, int v, int uSize, int vSize, int texSizeX, int texSizeY, float zLevel) {
		float uFact = 1f / texSizeX;
		float vFact = 1f / texSizeY;
		
		int uEnd = u + uSize;
		int vEnd = v + vSize;
		
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.addVertexWithUV(x + 0, y + height, zLevel, u * uFact, vEnd * vFact);
		t.addVertexWithUV(x + width, y + height, zLevel, uEnd * uFact, vEnd * vFact);
		t.addVertexWithUV(x + width, y + 0, zLevel, uEnd * uFact, v * vFact);
		t.addVertexWithUV(x + 0, y + 0, zLevel, u * uFact, v * vFact);
		t.draw();
	}
	
	public static void drawTexturedQuadFit(int x, int y, int width, int height) {
		drawTexturedQuadFit(x, y, width, height, getZLevel());
	}
	
	public static void drawTexturedQuadFit(int x, int y, int width, int height, float zLevel) {
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.addVertexWithUV(x + 0, y + height, zLevel, 0, 1);
		t.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
		t.addVertexWithUV(x + width, y + 0, zLevel, 1, 0);
		t.addVertexWithUV(x + 0, y + 0, zLevel, 0, 0);
		t.draw();
	}
	
	public static void unloadTexture(ResourceLocation loc) {
		TextureObject tex = SCReflector.instance.getTexturesMap(Minecraft.getMinecraft().renderEngine).remove(loc);
		if (tex != null) {
			glDeleteTextures(tex.getGlTextureId());
		}
	}

	private static float getZLevel() {
		return SCGuiAccessor.getZLevel(Minecraft.getMinecraft().currentScreen);
	}

}
