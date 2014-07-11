package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.meta.Subtypes;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

import java.util.List;

public class TypedItemBlock<BLOCK extends Block & HasSubtypes<TYPE>, TYPE extends Subtype> extends SCItemBlock<BLOCK> {

	public TypedItemBlock(int itemId, Block block) {
		super(itemId, block);
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(int blockID, CreativeTabs tab, List list) {
		Subtypes.getSubBlocksImpl(block, list);
	}

	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return block.getUnlocalizedName() + "." + block.subtypeProperty().value(stack).subtypeName();
	}

	@Override
	public Icon getIconFromDamage(int meta) {
		return block.getIcon(0, meta);
	}

}
