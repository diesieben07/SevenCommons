package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>A MetadataProperty, specialized for integer primitives.</p>
 *
 * @author diesieben07
 */
public interface IntProperty extends MetadataProperty<Integer> {

    /**
     * <p>Get the integer value of this property as represented by the given metadata value.</p>
     *
     * @param metadata the metadata
     * @return the value of this property
     */
    int intValue(int metadata);

    /**
     * <p>Get the integer value of this property as set for the Block at the given location.</p>
     *
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the value of this property
     */
    default int intValue(World world, int x, int y, int z) {
        return intValue(world.getBlockMetadata(x, y, z));
    }

    /**
     * <p>Get the integer value of this property as set in the given ItemStack.</p>
     *
     * @param stack the ItemStack
     * @return the value of this property
     */
    default int intValue(ItemStack stack) {
        return intValue(stack.getItemDamage());
    }

    /**
     * <p>Encodes the given value into the given metadata.</p>
     *
     * @param value        the value to store
     * @param previousMeta the previous metadata, possibly containing information about other properties
     * @return the new metadata value, now containing the given value
     */
    int toMeta(int value, int previousMeta);

    /**
     * <p>Apply the given value to the ItemStack.</p>
     *
     * @param value the value to store
     * @param stack the ItemStack
     * @return the same ItemStack, for convenience
     */
    default ItemStack apply(int value, ItemStack stack) {
        stack.setItemDamage(toMeta(value, stack.getItemDamage()));
        return stack;
    }

    /**
     * <p>Apply the given value to the Block at the given location in the world.</p>
     *
     * @param value the value to store
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     */
    default void apply(int value, World world, int x, int y, int z) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), 3);
    }

    /**
     * <p>Apply the given value to the Block at the given location in the world.</p>
     *
     * @param value       the value to store
     * @param world       the World
     * @param x           the x coordinate
     * @param y           the y coordinate
     * @param z           the z coordinate
     * @param notifyFlags the notify flags to pass to {@link net.minecraft.world.World#setBlockMetadataWithNotify(int, int, int, int, int)} (see there for documentation)
     */
    default void apply(int value, World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #intValue(int)}
     */
    @Override
    @Deprecated
    default Integer value(int metadata) {
        return intValue(metadata);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #intValue(net.minecraft.item.ItemStack)}
     */
    @Override
    @Deprecated
    default Integer value(ItemStack stack) {
        return intValue(stack);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #intValue(net.minecraft.world.World, int, int, int)}
     */
    @Override
    @Deprecated
    default Integer value(World world, int x, int y, int z) {
        return intValue(world, x, y, z);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #toMeta(int, int)}
     */
    @Deprecated
    @Override
    default int toMeta(Integer value, int previousMeta) {
        return toMeta((int) value, previousMeta);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(int, net.minecraft.item.ItemStack)}
     */
    @Deprecated
    @Override
    default ItemStack apply(Integer value, ItemStack stack) {
        return apply((int) value, stack);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(int, net.minecraft.world.World, int, int, int)}
     */
    @Deprecated
    @Override
    default void apply(Integer value, World world, int x, int y, int z) {
        apply((int) value, world, x, y, z);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(int, net.minecraft.world.World, int, int, int, int)}
     */
    @Deprecated
    @Override
    default void apply(Integer value, World world, int x, int y, int z, int notifyFlags) {
        apply((int) value, world, x, y, z, notifyFlags);
    }
}
