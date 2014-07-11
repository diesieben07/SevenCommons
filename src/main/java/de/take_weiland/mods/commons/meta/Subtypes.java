package de.take_weiland.mods.commons.meta;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * @author diesieben07
 */
public final class Subtypes {

    public static <TYPE extends Subtype> TYPE getType(HasSubtypes<TYPE> holder, ItemStack stack) {
        return getType(holder, stack.getItemDamage());
    }

    public static <TYPE extends Subtype> TYPE getType(HasSubtypes<TYPE> holder, int meta) {
        return holder.subtypeProperty().value(meta);
    }

    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, TYPE type) {
        return item.getUnlocalizedName() + "." + type.subtypeName();
    }

    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, ItemStack stack) {
        return name(item, item.subtypeProperty().value(stack));
    }

    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, int meta) {
        return name(item, item.subtypeProperty().value(meta));
    }

    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, TYPE type) {
        return block.getUnlocalizedName() + "." + type.subtypeName();
    }

    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, ItemStack stack) {
        return name(block, block.subtypeProperty().value(stack));
    }

    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, int meta) {
        return name(block, block.subtypeProperty().value(meta));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> void getSubItemsImpl(ITEM item, List list) {
        MetadataProperty<TYPE> property = item.subtypeProperty();

        for (TYPE subtype : property.values()) {
            list.add(new ItemStack(item, 1, property.toMeta(subtype, 0)));
        }
    }

    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> List<ItemStack> getSubItems(ITEM item) {
        MetadataProperty<TYPE> property = item.subtypeProperty();
        TYPE[] types = property.values();
        int len = types.length;
        ItemStack[] stacks = new ItemStack[len];

        for (int i = 0; i < len; ++i) {
            stacks[i] = new ItemStack(item, 1, property.toMeta(types[i], 0));
        }

        return Arrays.asList(stacks);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> void getSubBlocksImpl(BLOCK block, List list) {
        MetadataProperty<TYPE> property = block.subtypeProperty();

        for (TYPE subtype : property.values()) {
            list.add(new ItemStack(block, 1, property.toMeta(subtype, 0)));
        }
    }

    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> List<ItemStack> getSubBlocks(BLOCK block) {
        MetadataProperty<TYPE> property = block.subtypeProperty();
        TYPE[] types = property.values();
        int len = types.length;
        ItemStack[] stacks = new ItemStack[len];

        for (int i = 0; i < len; ++i) {
            stacks[i] = new ItemStack(block, 1, property.toMeta(types[i], 0));
        }

        return Arrays.asList(stacks);
    }

    private Subtypes() { }

}
