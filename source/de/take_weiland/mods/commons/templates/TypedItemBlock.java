package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.util.Multitypes;
import de.take_weiland.mods.commons.util.Names;

public class TypedItemBlock<T extends Block & Typed<R>, R extends Type<R>> extends AdvancedItemBlock<T> {

	public TypedItemBlock(int itemId, Block block) {
		super(itemId, block);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return Names.combine(block, Multitypes.getType(block, stack));
	}
	
	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

}
