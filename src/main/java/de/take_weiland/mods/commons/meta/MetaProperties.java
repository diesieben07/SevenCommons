package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Factory methods for MetadataProperties.</p>
 *
 * @author diesieben07
 * @see de.take_weiland.mods.commons.meta.MetadataProperty
 */
public final class MetaProperties {

	/**
	 * <p>Create a MetadataProperty, whose values are the constants of the given enum class.</p>
	 *
	 * @param startBit the first bit containing the data (0-31)
	 * @param clazz    the enum class
	 * @param <T>      the type of the values
	 * @return a MetadataProperty
	 */
	public static <T extends Enum<T>> MetadataProperty<T> newProperty(int startBit, Class<T> clazz) {
		return new EnumProperty<>(checkBit(startBit), checkNotNull(clazz, "clazz"));
	}

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
	public static <T> MetadataProperty<T> newProperty(int startBit, T... values) {
		checkBit(startBit);
		int len = checkNotNull(values, "values").length;
		checkArgument(len >= 2, "Need at least 2 elements");
		if (len == 2) {
			return new DualValueProperty<>(startBit, values[0], values[1]);
		} else {
			return new RegularArrayProperty<>(startBit, values);
		}
	}

	/**
	 * <p>Create a new MetadataProperty, representing a boolean value.</p>
	 *
	 * @param startBit the first bit containing the data (0-31)
	 * @return a BooleanProperty
	 */
	public static BooleanProperty newBooleanProperty(int startBit) {
		return BooleanPropertyImpl.get(checkBit(startBit));
	}

	/**
	 * <p>Create a new MetadataProperty, representing an integer value</p>
	 * <p>This property can hold values from 0 through 2<sup>bits</sup> - 1</p>
	 *
	 * @param startBit the first bit containing the data (0-31)
	 * @param bits     the number of bits to reserve (1-32)
	 * @return an IntProperty
	 */
	public static IntProperty newIntProperty(int startBit, int bits) {
		return new IntPropertyImpl(checkBit(startBit), checkBitCount(bits));
	}

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

	/**
	 * <p>Create a metadata value that represents all four values.</p>
	 *
	 * @param p1 the first MetadataProperty
	 * @param v1 the value for the first property
	 * @param p2 the second MetadataProperty
	 * @param v2 the value for the second property
	 * @param p3 the third MetadataProperty
	 * @param v3 the value for the third property
	 * @param p4 the fourth MetadataProperty
	 * @param v4 the value for the fourth property
	 * @return a metadata value
	 */
	public static <A, B, C, D> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4) {
		return apply(0, p1, v1, p2, v2, p3, v3, p4, v4);
	}

	/**
	 * <p>Apply all four values to the ItemStack.</p>
	 *
	 * @param p1 the first MetadataProperty
	 * @param v1 the value for the first property
	 * @param p2 the second MetadataProperty
	 * @param v2 the value for the second property
	 * @param p3 the third MetadataProperty
	 * @param v3 the value for the third property
	 * @param p4 the fourth MetadataProperty
	 * @param v4 the value for the fourth property
	 */
	public static <A, B, C, D> void apply(ItemStack stack, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4) {
		stack.setItemDamage(apply(stack.getItemDamage(), p1, v1, p2, v2, p3, v3, p4, v4));
	}

	/**
	 * <p>Apply all four value to the given metadata.</p>
	 *
	 * @param p1 the first MetadataProperty
	 * @param v1 the value for the first property
	 * @param p2 the second MetadataProperty
	 * @param v2 the value for the second property
	 * @param p3 the third MetadataProperty
	 * @param v3 the value for the third property
	 * @param p4 the fourth MetadataProperty
	 * @param v4 the value for the fourth property
	 * @return the new metadata
	 */
	public static <A, B, C, D> int apply(int base, MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4) {
		return p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, base))));
	}

	// going beyond 4 is madness, for anything more use the MetaBuilder

	/**
	 * @deprecated use {@link #newProperty(int, Class)} for Enum properties
	 */
	@Deprecated
	@SafeVarargs
	public static <T extends Enum<T>> MetadataProperty<T> newProperty(int shift, T... values) {
		throw new UnsupportedOperationException();
	}

	private static int checkBit(int bit) {
		checkArgument(bit >= 0 && bit <= 31, "Invalid start bit");
		return bit;
	}

	private static int checkBitCount(int bits) {
		checkArgument(bits >= 1 && bits <= 32, "Invalid bit count");
		return bits;
	}

}
