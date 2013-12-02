package de.take_weiland.mods.commons.client;

import static net.minecraftforge.common.ForgeDirection.DOWN;
import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.UP;
import static net.minecraftforge.common.ForgeDirection.WEST;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.SCGuiContainerAccessor;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import org.lwjgl.opengl.GL11;

public final class Rendering {

	private Rendering() { }
	
	public static final <T extends GuiContainer & ContainerGui<?>> void fillAreaWithIcon(Icon icon, int x, int y, int width, int height, T gui) {
		int xSize = SCGuiContainerAccessor.getSizeX(gui);
		int ySize = SCGuiContainerAccessor.getSizeY(gui);
		
		int cols = MathHelper.ceiling_float_int(width / 16F);
		int rows = MathHelper.ceiling_float_int(height / 16F);
//		System.out.println("cols: " + cols + " / rows: " + rows);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				gui.drawTexturedModelRectFromIcon(x + col * 16, y + row * 16, icon, 16, 16);
			}
		}
		gui.getMinecraft().renderEngine.bindTexture(gui.getTexture());
		gui.drawTexturedModalRect(x + width, y, x + width, y, xSize - (x + width), height);
		gui.drawTexturedModalRect(x, y + height, x, y + height, width, ySize - (y + height));
	}
	
	public static final <T extends GuiContainer & ContainerGui<?>> void drawFluidStack(IFluidTank tank, int x, int y, int width, int fullHeight, T gui) {
		FluidStack fluidStack = tank.getFluid();

		if (fluidStack != null) {
			Fluid fluid = fluidStack.getFluid();
			TextureManager engine = gui.getMinecraft().renderEngine;
			Icon fluidIcon = fluid.getStillIcon();
			int fluidHeight = MathHelper.ceiling_float_int((fluidStack.amount / (float)tank.getCapacity()) * fullHeight);

			GL11.glColor3f(1, 1, 1);
			engine.bindTexture(engine.getResourceLocation(fluid.getSpriteNumber()));
			fillAreaWithIcon(fluidIcon, x, y + fullHeight - fluidHeight, width, fluidHeight, gui);
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
	
	public static void drawRectWithZlevel(int xMin, int yMin, int xMax, int yMax, int zLevel, int color) {
		int temp;

		if (xMin < xMax) {
			temp = xMin;
			xMin = xMax;
			xMax = temp;
		}

		if (yMin < yMax) {
			temp = yMin;
			yMin = yMax;
			yMax = temp;
		}

		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(r, g, b, a);
		
		Tessellator t = Tessellator.instance;
		t.startDrawingQuads();
		t.addVertex(xMin, yMax, zLevel);
		t.addVertex(xMax, yMax, zLevel);
		t.addVertex(xMax, yMin, zLevel);
		t.addVertex(xMin, yMin, zLevel);
		t.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}

}
