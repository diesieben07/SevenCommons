package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class TypedItemBlock<BLOCK extends Block & HasSubtypes<TYPE>, TYPE extends Subtype> extends SCItemBlock<BLOCK> {

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
				+ block.subtypeProperty().value(stack).subtypeName();
	}

	@Override
	public Icon getIconFromDamage(int meta) {
		return block.getIcon(0, meta);
	}

}
