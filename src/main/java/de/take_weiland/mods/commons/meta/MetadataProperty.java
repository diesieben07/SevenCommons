package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>A property of a Block or Item, encoded in it's metadata / damage value.</p>
 * <p>A MetadataProperty takes up a certain number of bits in the metadata value, depending on how many distinct values it can represent.</p>
 * @author diesieben07
 */
public interface MetadataProperty<T> {

	/**
	 * <p>Get the value of this property as set in the given ItemStack.</p>
	 * @param stack the ItemStack
	 * @return the value of this property
	 */
	T value(ItemStack stack);

	/**
	 * <p>Get the value of this property as set for the Block at the given location.</p>
	 * @param world the World
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the value of this property
	 */
	T value(World world, int x, int y, int z);

	/**
	 * <p>Get the value of this property as represented by the given metadata value.</p>
	 * @param metadata the metadata
	 * @return the value of this property
	 */
	T value(int metadata);

	/**
	 * <p>Encodes the given value into the given metadata.</p>
	 * @param value the value to store
	 * @param previousMeta the previous metadata, possibly containing information about other properties
	 * @return the new metadata value, now containing the given value
	 */
	int toMeta(T value, int previousMeta);

	/**
	 * <p>Apply the given value to the ItemStack.</p>
	 * @param value the value to store
	 * @param stack the ItemStack
	 * @return the same ItemStack, for convenience
	 */
	ItemStack apply(T value, ItemStack stack);

	/**
	 * <p>Apply the given value to the Block at the given location in the world.</p>
	 * @param value the value to store
	 * @param world the World
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	void apply(T value, World world, int x, int y, int z);

	/**
	 * <p>Apply the given value to the Block at the given location in the world.</p>
	 * @param value the value to store
	 * @param world the World
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param notifyFlags the notify flags to pass to {@link net.minecraft.world.World#setBlockMetadataWithNotify(int, int, int, int, int)} (see there for documentation)
	 */
	void apply(T value, World world, int x, int y, int z, int notifyFlags);

	/**
	 * <p>Check if this property has distinct values (such as an Enum or a property based on an array).</p>
	 * @return true if this property has distinct values
	 */
	boolean hasDistinctValues();

	/**
	 * <p>Get all possible values of this property. This method is only valid if {@link #hasDistinctValues()} is true.</p>
	 * @return all possible values of this property
	 * @throws java.lang.UnsupportedOperationException if this method doesn't have distinct values
	 */
	T[] values();

}
