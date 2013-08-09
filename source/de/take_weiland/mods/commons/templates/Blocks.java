package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.Inventories;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class Blocks {

	private Blocks() { }
	
	public static final void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			genericBreak(world.getBlockTileEntity(x, y, z));
		}
	}
	
	public static final void genericBreak(TileEntity te) {
		Inventories.spillIfInventory(te);
	}

}
