package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Factory methods for MetadataProperties.</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.meta.MetadataProperty
 */
public final class MetaProperties {

    /**
     * <p>Create a metadata value that represents both the first and then second value.</p>
     *
     * @param p1 the first MetadataProperty
     * @param v1 the value for the first property
     * @param p2 the second MetadataProperty
     * @param v2 the value for the second property
     * @return a metadata value
     */
    public static <A, B> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2) {
        return apply(0, p1, v1, p2, v2);
    }

    /**
     * <p>Apply both the first and the second value to the ItemStack.</p>
     *
     * @param p1 the first MetadataProperty
     * @param v1 the value for the first property
     * @param p2 the second MetadataProperty
     * @param v2 the value for the second property
     */
    public static <A, B> void apply(ItemStack stack, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2) {
        stack.setItemDamage(apply(stack.getItemDamage(), p1, v1, p2, v2));
    }

    /**
     * <p>Apply both the first and the second value to the given metadata.</p>
     *
     * @param meta the original metadata
     * @param p1   the first MetadataProperty
     * @param v1   the value for the first property
     * @param p2   the second MetadataProperty
     * @param v2   the value for the second property
     * @return the new metadata
     */
    public static <A, B> int apply(int meta, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2) {
        return p2.toMeta(v2, p1.toMeta(v1, meta));
    }

    /**
     * <p>Create a metadata value that represents all three values.</p>
     *
     * @param p1 the first MetadataProperty
     * @param v1 the value for the first property
     * @param p2 the second MetadataProperty
     * @param v2 the value for the second property
     * @param p3 the third MetadataProperty
     * @param v3 the value for the third property
     * @return a metadata value
     */
    public static <A, B, C> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3) {
        return apply(0, p1, v1, p2, v2, p3, v3);
    }

    /**
     * <p>Apply all three values to the ItemStack.</p>
     *
     * @param p1 the first MetadataProperty
     * @param v1 the value for the first property
     * @param p2 the second MetadataProperty
     * @param v2 the value for the second property
     * @param p3 the third MetadataProperty
     * @param v3 the value for the third property
     */
    public static <A, B, C> void apply(ItemStack stack, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3) {
        stack.setItemDamage(apply(stack.getItemDamage(), p1, v1, p2, v2, p3, v3));
    }

    /**
     * <p>Apply all three value to the given metadata.</p>
     *
     * @param p1 the first MetadataProperty
     * @param v1 the value for the first property
     * @param p2 the second MetadataProperty
     * @param v2 the value for the second property
     * @param p3 the third MetadataProperty
     * @param v3 the value for the third property
     * @return the new metadata
     */
    public static <A, B, C> int apply(int meta, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3) {
        return p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, meta)));
    }

    // going beyond 3 is madness, for anything more use the MetaBuilder

    static int checkBit(int bit) {
        checkArgument(bit >= 0 && bit <= 31, "Invalid start bit");
        return bit;
    }

    static int checkBitCount(int bits) {
        checkArgument(bits >= 1 && bits <= 32, "Invalid bit count");
        return bits;
    }

}
