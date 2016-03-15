package de.take_weiland.mods.commons.meta;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * <p>When implemented on a Block or Item specifies the subtypes of that Block or Item.</p>
 *
 * @author diesieben07
 */
public interface HasSubtypes<T extends Subtype> {

    /**
     * <p>Return the MetadataProperty that represents the subtypes of ths Block or Item.</p>
     *
     * @return a MetadataProperty
     */
    MetadataProperty<T> subtypeProperty();

    /**
     * <p>Get the type that corresponds to the given metadata.</p>
     *
     * @param meta the metadata
     * @return the type
     */
    default T getType(int meta) {
        return subtypeProperty().value(meta);
    }

    /**
     * <p>Get the type that corresponds to the given ItemStack.</p>
     *
     * @param stack the ItemStack
     * @return the type
     */
    default T getType(ItemStack stack) {
        return subtypeProperty().value(stack);
    }

    /**
     * <p>Apply the given type to the given ItemStack.</p>
     *
     * @param stack the ItemStack
     * @param type  the type
     * @return the ItemStack, for convenience
     */
    default ItemStack setType(ItemStack stack, T type) {
        return subtypeProperty().apply(type, stack);
    }

    /**
     * <p>Apply the given type to the given metadata.</p>
     *
     * @param meta the metadata
     * @param type the type
     * @return the new metadata
     */
    default int setType(int meta, T type) {
        return subtypeProperty().toMeta(type, meta);
    }

    /**
     * <p>Create an ItemStack of the given type.</p>
     *
     * @param type the type
     * @return an ItemStack
     */
    default ItemStack getStack(T type) {
        return getStack(type, 1);
    }

    /**
     * <p>Create an ItemStack of the given type and size.</p>
     *
     * @param type the type
     * @param size the size
     * @return an ItemStack
     */
    default ItemStack getStack(T type, int size) {
        if (this instanceof Block) {
            return new ItemStack((Block) this, size, subtypeProperty().toMeta(type));
        } else {
            try {
                return new ItemStack((Item) this, size, subtypeProperty().toMeta(type));
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException("HasSubtypes implemented on something that is not Item or Block!");
            }
        }
    }

    /**
     * <p>Get a combined name for the given Item and it's subtype.</p>
     * <p>The result of this method is equivalent to {@code item.getUnlocalizedName() + "." + type.subtypeName()}.</p>
     *
     * @param item the Item
     * @param type the subtype
     * @return a combined name
     */
    static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, TYPE type) {
        return item.getUnlocalizedName() + "." + type.subtypeName();
    }

    /**
     * <p>Get a combined name for the given Item and it's subtype as specified by the given ItemStack.</p>
     * <p>The result of this method is equivalent to {@code Subtypes.name(item, item.subtypeProperty().value(stack)}.</p>
     *
     * @param item  the Item
     * @param stack the ItemStack
     * @return a combined name
     */
    static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, ItemStack stack) {
        return name(item, item.subtypeProperty().value(stack));
    }

    /**
     * <p>Get a combined name for the given Item and it's subtype as specified by the given metadata.</p>
     * <p>The result of this method is equivalent to {@code Subtypes.name(item, item.subtypeProperty().value(meta)}.</p>
     *
     * @param item the Item
     * @param meta the metadata
     * @return a combined name
     */
    static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, int meta) {
        return name(item, item.subtypeProperty().value(meta));
    }

    /**
     * <p>Get a combined name for the given Block and it's subtype.</p>
     * <p>The result of this method is equivalent to {@code block.getUnlocalizedName() + "." + type.subtypeName()}.</p>
     *
     * @param block the Block
     * @param type  the subtype
     * @return a combined name
     */
    static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, TYPE type) {
        return block.getUnlocalizedName() + "." + type.subtypeName();
    }

    /**
     * <p>Get a combined name for the given Block and it's subtype as specified by the given ItemStack.</p>
     * <p>The result of this method is equivalent to {@code Subtypes.name(block, block.subtypeProperty().value(stack)}.</p>
     *
     * @param block the Block
     * @param stack the ItemStack
     * @return a combined name
     */
    static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, ItemStack stack) {
        return name(block, block.subtypeProperty().value(stack));
    }

    /**
     * <p>Get a combined name for the given Block and it's subtype as specified by the given metadata.</p>
     * <p>The result of this method is equivalent to {@code Subtypes.name(block, block.subtypeProperty().value(meta)}.</p>
     *
     * @param block the Block
     * @param meta  the metadata
     * @return a combined name
     */
    static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, int meta) {
        return name(block, block.subtypeProperty().value(meta));
    }

    /**
     * <p>An implementation for {@link Item#getSubItems(Item, CreativeTabs, List)}.</p>
     *
     * @param item the Item
     * @param list the List to add ItemStacks to
     */
    static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> void getSubItemsImpl(ITEM item, List<ItemStack> list) {
        MetadataProperty<TYPE> property = item.subtypeProperty();

        for (TYPE subtype : property.values()) {
            list.add(new ItemStack(item, 1, property.toMeta(subtype, 0)));
        }
    }

    /**
     * <p>An implementation for {@link Block#getSubBlocks(Item, CreativeTabs, List)}.</p>
     *
     * @param block the Block
     * @param list  the List to add ItemStacks to
     */
    static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> void getSubBlocksImpl(BLOCK block, List<ItemStack> list) {
        MetadataProperty<TYPE> property = block.subtypeProperty();
        for (TYPE type : property.values()) {
            list.add(property.apply(type, new ItemStack(block)));
        }
    }

}
