package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

public class TypedItemBlock<BLOCK extends Block & HasSubtypes<TYPE>, TYPE extends Subtype> extends ItemBlock {

    public TypedItemBlock(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        //noinspection unchecked
        HasSubtypes.getSubBlocksImpl((BLOCK) blockInstance, list);
    }

    @Override
    public int getMetadata(int itemMeta) {
        return itemMeta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        //noinspection unchecked
        return blockInstance.getUnlocalizedName() + "." + ((BLOCK) blockInstance).subtypeProperty().value(stack).subtypeName();
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return blockInstance.getIcon(0, meta);
    }

    @Override
    public final String getUnlocalizedNameInefficiently(ItemStack stack) {
        return getUnlocalizedName(stack);
    }
}
