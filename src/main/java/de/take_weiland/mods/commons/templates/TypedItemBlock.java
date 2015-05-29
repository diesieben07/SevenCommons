package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;

public class TypedItemBlock<BLOCK extends Block & HasSubtypes<TYPE>, TYPE extends Subtype> extends SCItemBlock {

    public TypedItemBlock(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        //noinspection unchecked
        HasSubtypes.getSubBlocksImpl((BLOCK) field_150939_a, list);
    }

    @Override
    public int getMetadata(int itemMeta) {
        return itemMeta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        //noinspection unchecked
        return field_150939_a.getUnlocalizedName() + "." + ((BLOCK) field_150939_a).subtypeProperty().value(stack).subtypeName();
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return field_150939_a.getIcon(0, meta);
    }

}
