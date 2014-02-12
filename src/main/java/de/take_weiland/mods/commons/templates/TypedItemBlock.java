package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.templates.Metadata.BlockMeta;
import de.take_weiland.mods.commons.util.Multitypes;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class TypedItemBlock<BLOCK extends Block & HasMetadata<TYPE>, TYPE extends BlockMeta> extends SCItemBlock<BLOCK> {

	public TypedItemBlock(int itemId, Block block) {
		super(itemId, block);
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + Multitypes.getType(block, stack).unlocalizedName();
	}

}
