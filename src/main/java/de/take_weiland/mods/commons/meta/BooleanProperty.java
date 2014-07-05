package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>A metadata property, specialized for boolean primitives.</p>
 *
 * @author diesieben07
 */
public interface BooleanProperty extends MetadataProperty<Boolean> {

	/**
	 * <p>Get the boolean value of this property as represented by the given metadata value.</p>
	 * @param metadata the metadata
	 * @return the value of this property
	 */
	boolean booleanValue(int metadata);

	/**
	 * <p>Get the boolean value of this property as set in the given ItemStack.</p>
	 * @param stack the ItemStack
	 * @return the value of this property
	 */
	boolean booleanValue(ItemStack stack);

	/**
	 * <p>Get the boolean value of this property as set for the Block at the given location.</p>
	 * @param world the World
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the value of this property
	 */
	boolean booleanValue(World world, int x, int y, int z);

	/**
	 * <p>Encodes the given value into the given metadata.</p>
	 * @param value the value to store
	 * @param previousMeta the previous metadata, possibly containing information about other properties
	 * @return the new metadata value, now containing the given value
	 */
	int toMeta(boolean value, int previousMeta);

	/**
	 * {@inheritDoc}
	 * @deprecated use the specialized version {@link #booleanValue(net.minecraft.item.ItemStack)}
	 */
	@Deprecated
	@Override
	Boolean value(ItemStack stack);

	/**
	 * {@inheritDoc}
	 * @deprecated use the specialized version {@link #booleanValue(net.minecraft.world.World, int, int, int)}
	 */
	@Deprecated
	@Override
	Boolean value(World world, int x, int y, int z);

	/**
	 * {@inheritDoc}
	 * @deprecated use the specialized version {@link #booleanValue(int)}
	 */
	@Deprecated
	@Override
	Boolean value(int metadata);

	/**
	 * {@inheritDoc}
	 * @deprecated use the specialized version {@link #toMeta(boolean, int)}
	 */
	@Deprecated
	@Override
	int toMeta(Boolean value, int previousMeta);
}
