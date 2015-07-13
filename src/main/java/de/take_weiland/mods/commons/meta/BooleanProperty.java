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
     *
     * @param metadata the metadata
     * @return the value of this property
     */
    boolean booleanValue(int metadata);

    /**
     * <p>Get the boolean value of this property as set in the given ItemStack.</p>
     *
     * @param stack the ItemStack
     * @return the value of this property
     */
    default boolean booleanValue(ItemStack stack) {
        return booleanValue(stack.getMetadata());
    }

    /**
     * <p>Get the boolean value of this property as set for the Block at the given location.</p>
     *
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the value of this property
     */
    default boolean booleanValue(World world, int x, int y, int z) {
        return booleanValue(world.getBlockMetadata(x, y, z));
    }

    /**
     * <p>Encodes the given value into the given metadata.</p>
     *
     * @param value        the value to store
     * @param previousMeta the previous metadata, possibly containing information about other properties
     * @return the new metadata value, now containing the given value
     */
    int toMeta(boolean value, int previousMeta);

    /**
     * <p>Apply the given value to the ItemStack.</p>
     *
     * @param value the value to store
     * @param stack the ItemStack
     * @return the same ItemStack, for convenience
     */
    default ItemStack apply(boolean value, ItemStack stack) {
        stack.setMetadata(toMeta(value, stack.getMetadata()));
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
    default void apply(boolean value, World world, int x, int y, int z) {
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
    default void apply(boolean value, World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #booleanValue(net.minecraft.item.ItemStack)}
     */
    @Deprecated
    @Override
    default Boolean value(ItemStack stack) {
        return booleanValue(stack);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #booleanValue(net.minecraft.world.World, int, int, int)}
     */
    @Deprecated
    @Override
    default Boolean value(World world, int x, int y, int z) {
        return booleanValue(world, x, y, z);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #booleanValue(int)}
     */
    @Deprecated
    @Override
    default Boolean value(int metadata) {
        return booleanValue(metadata);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #toMeta(boolean, int)}
     */
    @Deprecated
    @Override
    default int toMeta(Boolean value, int previousMeta) {
        return toMeta((boolean) value, previousMeta);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(boolean, net.minecraft.item.ItemStack)}
     */
    @Deprecated
    @Override
    default ItemStack apply(Boolean value, ItemStack stack) {
        return apply((boolean) value, stack);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(boolean, net.minecraft.world.World, int, int, int)}
     */
    @Deprecated
    @Override
    default void apply(Boolean value, World world, int x, int y, int z) {
        apply((boolean) value, world, x, y, z);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use the specialized version {@link #apply(boolean, net.minecraft.world.World, int, int, int, int)}
     */
    @Deprecated
    @Override
    default void apply(Boolean value, World world, int x, int y, int z, int notifyFlags) {
        apply((boolean) value, world, x, y, z, notifyFlags);
    }
}
