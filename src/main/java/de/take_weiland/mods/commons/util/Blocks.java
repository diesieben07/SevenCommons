package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.item.ItemStacks;
import de.take_weiland.mods.commons.item.Items;
import de.take_weiland.mods.commons.templates.HasMetadata;
import de.take_weiland.mods.commons.templates.Metadata.BlockMeta;
import de.take_weiland.mods.commons.templates.SCItemBlock;
import de.take_weiland.mods.commons.templates.TypedItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

public final class Blocks {

	private Blocks() { }
	
	public static void init(Block block, String baseName) {
		init(block, baseName, block instanceof HasMetadata ? TypedItemBlock.class : SCItemBlock.class);
	}
	
	@SuppressWarnings("unchecked")
	public static void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		block.setTextureName(Items.getIconName(modId, baseName));
		block.setUnlocalizedName(modId + "." +  baseName);
		
		GameRegistry.registerBlock(block, itemClass, baseName);
		
		if (block instanceof HasMetadata) {
			ItemStacks.registerAll(((HasMetadata<? extends BlockMeta>) block).getTypes(), baseName, ItemStacks.BLOCK_GET_STACK);
		}
	}
	
	public static void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			Inventories.spillIfInventory(world.getBlockTileEntity(x, y, z));
		}
	}
	
}
