package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>A builder for easy combination of metadata properties into a single value.</p>
 * <p>Builder instances can be reused with the {@link #reset(int)} method.</p>
 *
 * <p>Example usage:<pre>
 *     <code>int meta = MetadataProperty.newBuilder()
 *                          .set(someProperty, value)
 *                          .set(otherProperty, value)
 *                          .toMeta()</code>
 * </pre></p>
 *
 * @author diesieben07
 */
public final class MetaBuilder {

    MetaBuilder(int initialValue) {
        meta = initialValue;
    }

    private int meta;

    /**
     * <p>Set the given property to the given value in this builder's result.</p>
     *
     * @param property the MetadataProperty
     * @param value    the value of the property
     * @return {@code this}, for convenience
     */
    public <T> MetaBuilder set(MetadataProperty<? super T> property, T value) {
        meta = property.toMeta(value, meta);
        return this;
    }

    /**
     * <p>Set the given property to the given value in this builder's result.</p>
     *
     * @param property the MetadataProperty
     * @param value    the value of the property
     * @return {@code this}, for convenience
     */
    public MetaBuilder set(BooleanProperty property, boolean value) {
        meta = property.toMeta(value, meta);
        return this;
    }

    /**
     * <p>Set the given property to the given value in this builder's result.</p>
     *
     * @param property the MetadataProperty
     * @param value    the value of the property
     * @return {@code this}, for convenience
     */
    public MetaBuilder set(IntProperty property, int value) {
        meta = property.toMeta(value, meta);
        return this;
    }

    /**
     * <p>Create this builder's result as a metadata value.</p>
     *
     * @return the metadata result
     */
    public int toMeta() {
        return meta;
    }

    /**
     * <p>Apply this builder's result to the given ItemStack.</p>
     *
     * @param stack the ItemStack
     */
    public void apply(ItemStack stack) {
        stack.setMetadata(toMeta());
    }

    /**
     * <p>Apply this builder's result to the given block location.</p>
     *
     * @param world       the World
     * @param x           the x coordinate
     * @param y           the y coordinate
     * @param z           the z coordinate
     * @param notifyFlags the notify flags to pass to {@link net.minecraft.world.World#setBlockMetadataWithNotify(int, int, int, int, int)} (see there for documentation)
     */
    public void apply(World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(), notifyFlags);
    }

    /**
     * <p>Apply this builder's result to the given block location.</p>
     *
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     */
    public void apply(World world, int x, int y, int z) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(), 3);
    }

    /**
     * <p>Reset this builder's value to 0 so that it can be reused.</p>
     *
     * @return {@code this}, for convenience
     */
    public MetaBuilder reset() {
        meta = 0;
        return this;
    }

    /**
     * <p>Reset this builder's value to the given value so that it can be reused.</p>
     *
     * @param value the initial value
     * @return {@code this}, for convenience
     */
    public MetaBuilder reset(int value) {
        meta = value;
        return this;
    }
}
