package de.take_weiland.mods.commons.templates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.util.Multitypes;

public class TypedItemBlock<T extends Block & Typed<R>, R extends Type<R>> extends AdvancedItemBlock<T> {

	public TypedItemBlock(int itemId, Block block) {
		super(itemId, block);
		setHasSubtypes(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemDisplayName(ItemStack stack) {
		return I18n.getString(Multitypes.name(Multitypes.getType(block, stack)));
	}

	@Override
	public int getMetadata(int itemMeta) {
		return itemMeta;
	}

}
