package de.take_weiland.mods.commons.meta;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * <p>Utilities for Blocks / Items with subtypes.</p>
 * @see HasSubtypes
 * @author diesieben07
 */
public final class Subtypes {

	/**
	 * <p>Get the subtype of the given ItemStack.</p>
	 * @param holder the Block or Item
	 * @param stack the ItemStack
	 * @return the subtype
	 */
	public static <TYPE extends Subtype> TYPE getType(HasSubtypes<TYPE> holder, ItemStack stack) {
		return getType(holder, stack.getItemDamage());
	}

	/**
	 * <p>Get the subtype of the given metadata.</p>
	 * @param holder the Block or Item
	 * @param meta the metadata
	 * @return the subtype
	 */
	public static <TYPE extends Subtype> TYPE getType(HasSubtypes<TYPE> holder, int meta) {
		return holder.subtypeProperty().value(meta);
	}

	/**
	 * <p>Get a combined name for the given Item and it's subtype.</p>
	 * <p>The result of this method is equivalent to {@code item.getUnlocalizedName() + "." + type.subtypeName()}.</p>
	 * @param item the Item
	 * @param type the subtype
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, TYPE type) {
		return item.getUnlocalizedName() + "." + type.subtypeName();
	}

	/**
	 * <p>Get a combined name for the given Item and it's subtype as specified by the given ItemStack.</p>
	 * <p>The result of this method is equivalent to {@code Subtypes.name(item, item.subtypeProperty().value(stack)}.</p>
	 * @param item the Item
	 * @param stack the ItemStack
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, ItemStack stack) {
		return name(item, item.subtypeProperty().value(stack));
	}

	/**
	 * <p>Get a combined name for the given Item and it's subtype as specified by the given metadata.</p>
	 * <p>The result of this method is equivalent to {@code Subtypes.name(item, item.subtypeProperty().value(meta)}.</p>
	 * @param item the Item
	 * @param meta the metadata
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> String name(ITEM item, int meta) {
		return name(item, item.subtypeProperty().value(meta));
	}

	/**
	 * <p>Get a combined name for the given Block and it's subtype.</p>
	 * <p>The result of this method is equivalent to {@code block.getUnlocalizedName() + "." + type.subtypeName()}.</p>
	 * @param block the Block
	 * @param type the subtype
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, TYPE type) {
		return block.getUnlocalizedName() + "." + type.subtypeName();
	}

	/**
	 * <p>Get a combined name for the given Block and it's subtype as specified by the given ItemStack.</p>
	 * <p>The result of this method is equivalent to {@code Subtypes.name(block, block.subtypeProperty().value(stack)}.</p>
	 * @param block the Block
	 * @param stack the ItemStack
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, ItemStack stack) {
		return name(block, block.subtypeProperty().value(stack));
	}

	/**
	 * <p>Get a combined name for the given Block and it's subtype as specified by the given metadata.</p>
	 * <p>The result of this method is equivalent to {@code Subtypes.name(block, block.subtypeProperty().value(meta)}.</p>
	 * @param block the Block
	 * @param meta the metadata
	 * @return a combined name
	 */
	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> String name(BLOCK block, int meta) {
		return name(block, block.subtypeProperty().value(meta));
	}

	/**
	 * <p>An implementation for {@link Item#getSubItems(int, CreativeTabs, List)}.</p>
	 * <p>The {@code list} parameter is intentionally declared as a raw-type to be able to call this method from the
	 * erased version in the Item class.</p>
	 * @param item the Item
	 * @param list the List to add ItemStacks to
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> void getSubItemsImpl(ITEM item, List list) {
		MetadataProperty<TYPE> property = item.subtypeProperty();

		for (TYPE subtype : property.values()) {
			list.add(new ItemStack(item, 1, property.toMeta(subtype, 0)));
		}
	}

	/**
	 * <p>An implementation for {@link Block#getSubBlocks(int, CreativeTabs, List)}.</p>
	 * <p>The {@code list} parameter is intentionally declared as a raw-type to be able to call this method from the
	 * erased version in the Block class.</p>
	 * @param block the Block
	 * @param list the List to add ItemStacks to
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> void getSubBlocksImpl(BLOCK block, List list) {
		MetadataProperty<TYPE> property = block.subtypeProperty();

		for (TYPE subtype : property.values()) {
			list.add(new ItemStack(block, 1, property.toMeta(subtype, 0)));
		}
	}

	private Subtypes() { }

}
