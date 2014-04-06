package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class TypedItemBlock<BLOCK extends Block & HasSubtypes<TYPE>, TYPE extends Enum<TYPE> & Subtype> extends SCItemBlock<BLOCK> {

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
		return block.getUnlocalizedName()
				+ "."
				+ block.subtypeProperty().value(stack).unlocalizedName();
	}

}
