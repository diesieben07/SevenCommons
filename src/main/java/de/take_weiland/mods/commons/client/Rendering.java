package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.util.MiscUtil;
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
	
	public static final void fillAreaWithIcon(Icon icon, int x, int y, int width, int height) {
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
	
	public static final void drawFluidStack(IFluidTank tank, int x, int y, int width, int fullHeight) {
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
		drawColoredRect(x, y, width, height, color, getZLevel());
	}
	
	public static void drawColoredRect(int x, int y, int width, int height, int color, float zLevel) {
		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		
		glEnable(GL_BLEND);
		glDisable(GL_TEXTURE_2D);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glColor4f(r, g, b, a);
		
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
		TextureObject tex = MiscUtil.getReflector().getTexturesMap(Minecraft.getMinecraft().renderEngine).remove(loc);
		if (tex != null) {
			glDeleteTextures(tex.getGlTextureId());
		}
	}

	private static float getZLevel() {
		return SCGuiAccessor.getZLevel(Minecraft.getMinecraft().currentScreen);
	}

}
