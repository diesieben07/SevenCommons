package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.util.Blocks;

public class TypedItemBlock extends ItemBlock {

	private final Block block;
	
	public TypedItemBlock(int itemId, Block block) {
		super(itemId);
		this.block = block;
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedNameImpl(stack);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Block & Typed<E>, E extends Type> String getUnlocalizedNameImpl(ItemStack stack) {
		return Blocks.getUnlocalizedName((T)block, stack);
	}

	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

}
