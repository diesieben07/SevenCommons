package de.take_weiland.mods.commons.meta;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>A property of a Block or Item, encoded in it's metadata / damage value.</p>
 * <p>A MetadataProperty takes up a certain number of bits in the metadata value, depending on how many distinct values it can represent.</p>
 *
 * @author diesieben07
 */
public interface MetadataProperty<T> {

    /**
     * <p>Create a MetadataProperty, whose values are given those of the given array.</p>
     * <p>The array must not be modified after being passed to this method.</p>
     *
     * @param startBit the first bit containing the data (0-31)
     * @param values   the values for this property
     * @param <T>      the type of the values
     * @return a MetadataProperty
     */
    @SafeVarargs
    static <T> MetadataProperty<T> newProperty(int startBit, T... values) {
        MetaProperties.checkBit(startBit);
        int len = checkNotNull(values, "values").length;
        checkArgument(len >= 2, "Need at least 2 elements");

        return new RegularArrayProperty<>(startBit, values);
    }

    /**
     * <p>Create a MetadataProperty, whose values are the constants of the given enum class.</p>
     *
     * @param startBit the first bit containing the data (0-31)
     * @param clazz    the enum class
     * @param <T>      the type of the values
     * @return a MetadataProperty
     */
    static <T extends Enum<T>> MetadataProperty<T> newProperty(int startBit, Class<T> clazz) {
        return new EnumProperty<>(MetaProperties.checkBit(startBit), checkNotNull(clazz, "clazz"));
    }

    /**
     * @deprecated use {@link #newProperty(int, Class)} for Enum properties
     */
    @Deprecated
    @SafeVarargs
    static <T extends Enum<T>> MetadataProperty<T> newProperty(int startBit, T... values) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>Create a new MetadataProperty, representing a boolean value.</p>
     *
     * @param startBit the bit containing the data (0-31)
     * @return a BooleanProperty
     */
    static BooleanProperty newBooleanProperty(int startBit) {
        return BooleanPropertyImpl.get(MetaProperties.checkBit(startBit));
    }

    /**
     * <p>Create a new MetadataProperty, representing an integer value</p>
     * <p>This property can hold values from 0 through 2<sup>bits</sup> - 1</p>
     *
     * @param startBit the first bit containing the data (0-31)
     * @param bits     the number of bits to reserve (1-32)
     * @return an IntProperty
     */
    static IntProperty newIntProperty(int startBit, int bits) {
        return new IntPropertyImpl(MetaProperties.checkBit(startBit), MetaProperties.checkBitCount(bits));
    }

    /**
     * <p>Get the value of this property as represented by the given metadata value.</p>
     *
     * @param metadata the metadata
     * @return the value of this property
     */
    T value(int metadata);

    /**
     * <p>Get the value of this property as set in the given ItemStack.</p>
     *
     * @param stack the ItemStack
     * @return the value of this property
     */
    default T value(ItemStack stack) {
        return value(stack.getItemDamage());
    }

    /**
     * <p>Get the value of this property as set for the Block at the given location.</p>
     *
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the value of this property
     */
    default T value(World world, int x, int y, int z) {
        return value(world.getBlockMetadata(x, y, z));
    }

    default int toMeta(T value) {
        return toMeta(value, 0);
    }

    /**
     * <p>Encodes the given value into the given metadata.</p>
     *
     * @param value        the value to store
     * @param previousMeta the previous metadata, possibly containing information about other properties
     * @return the new metadata value, now containing the given value
     */
    int toMeta(T value, int previousMeta);

    /**
     * <p>Apply the given value to the ItemStack.</p>
     *
     * @param value the value to store
     * @param stack the ItemStack
     * @return the same ItemStack, for convenience
     */
    default ItemStack apply(T value, ItemStack stack) {
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
    default void apply(T value, World world, int x, int y, int z) {
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
    default void apply(T value, World world, int x, int y, int z, int notifyFlags) {
        world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
    }

    /**
     * <p>Check if this property has distinct values (such as an Enum or a property based on an array).</p>
     *
     * @return true if this property has distinct values
     */
    boolean hasDistinctValues();

    /**
     * <p>Get all possible values of this property. This method is only valid if {@link #hasDistinctValues()} is true.</p>
     *
     * @return all possible values of this property
     * @throws java.lang.UnsupportedOperationException if this method doesn't have distinct values
     */
    Set<T> values();

    /**
     * <p>Create a mutable Map for mapping values of this property to objects of type {@code V}.</p>
     * <p>It is encouraged that implementations of this method choose an appropriate Map type based on the key type.</p>
     *
     * @return a newly created, empty mutable Map
     */
    default <V> Map<T, V> createMap() {
        if (hasDistinctValues()) {
            return Maps.newHashMapWithExpectedSize(values().size());
        } else {
            return Maps.newHashMap();
        }
    }

}
