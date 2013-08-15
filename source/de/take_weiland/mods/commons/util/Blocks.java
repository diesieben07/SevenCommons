package de.take_weiland.mods.commons.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public final class Blocks {

	private Blocks() { }
	
	public static final void init(Block block, String baseName) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		block.func_111022_d(Items.getIconName(modId, baseName)); // set icon name
		block.setUnlocalizedName(Items.getLanguageKey(modId, baseName));
		
		GameRegistry.registerBlock(block, baseName);
	}
	
	public static final void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			genericBreak(world.getBlockTileEntity(x, y, z));
		}
	}
	
	public static final void genericBreak(TileEntity te) {
		Inventories.spillIfInventory(te);
	}

}
