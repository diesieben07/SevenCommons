package de.take_weiland.mods.commons.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;

public final class Blocks {

	private Blocks() { }
	
	public static final void init(Block block, String baseName) {
		init(block, baseName, ItemBlock.class);
	}
	
	public static final void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		block.func_111022_d(Items.getIconName(modId, baseName)); // set icon name
		block.setUnlocalizedName(Items.getLanguageKey(modId, baseName));
		
		GameRegistry.registerBlock(block, itemClass, baseName);
	}
	
	public static final void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			genericBreak(world.getBlockTileEntity(x, y, z));
		}
	}
	
	public static final void genericBreak(TileEntity te) {
		Inventories.spillIfInventory(te);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T extends Block & Typed<?>> void addSubtypes(T block, List stacks) {
		for (Type type : block.getTypes()) {
			stacks.add(new ItemStack(block, 1, type.getMeta()));
		}
	}

}
