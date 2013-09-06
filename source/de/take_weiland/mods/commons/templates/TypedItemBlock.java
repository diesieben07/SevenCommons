package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.util.Blocks;

public class TypedItemBlock<T extends Block & Typed<R>, R extends Type> extends AdvancedItemBlock<T> {

	public TypedItemBlock(int itemId, Block block) {
		super(itemId, block);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return Blocks.getUnlocalizedName(block, stack);
	}
	
	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

}
