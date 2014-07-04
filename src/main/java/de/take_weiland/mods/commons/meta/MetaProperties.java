package de.take_weiland.mods.commons.meta;

import com.google.common.primitives.Ints;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Factory methods for MetadataProperties.</p>
 * @see de.take_weiland.mods.commons.meta.MetadataProperty
 * @author diesieben07
 */
public final class MetaProperties {

	/**
	 * <p>Create a MetadataProperty, whose values are the constants of the given enum class.</p>
	 * @param shift the shift
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T extends Enum<T>> MetadataProperty<T> newProperty(int shift, Class<T> clazz) {
		return new EnumProperty<>(shift, checkNotNull(clazz, "clazz"));
	}

	@SafeVarargs
	public static <T> MetadataProperty<T> newProperty(int shift, T... values) {
		checkShift(shift);
		if (checkNotNull(values, "values").length == 2) {
			return new DualValueProperty<>(shift, values[0], values[1]);
		} else {
			return new RegularArrayProperty<>(shift, values);
		}
	}

	public static BooleanProperty newBooleanProperty(int shift) {
		return BooleanPropertyImpl.get(checkShift(shift));
	}

	public static IntProperty newIntProperty(int shift, int bits) {
		return new IntPropertyImpl(shift, bits);
	}

	public static <A, B> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2) {
		return p2.toMeta(v2, p1.toMeta(v1, 0));
	}

	public static <A, B, C> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3) {
		return p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0)));
	}

	public static <A, B, C, D> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4) {
		return p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0))));
	}

	public static <A, B, C, D, E> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4, MetadataProperty<? super E> p5, E v5) {
		return p5.toMeta(v5, p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0)))));
	}

	public static <A, B, C, D, E, F> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4, MetadataProperty<? super E> p5, E v5, MetadataProperty<? super F> p6, F v6) {
		return p6.toMeta(v6, p5.toMeta(v5, p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0))))));
	}

	public static <A, B, C, D, E, F, G> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4, MetadataProperty<? super E> p5, E v5, MetadataProperty<? super F> p6, F v6, MetadataProperty<? super G> p7, G v7) {
		return p7.toMeta(v7, p6.toMeta(v6, p5.toMeta(v5, p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0)))))));
	}

	public static <A, B, C, D, E, F, G, H> int combine(MetadataProperty<? super A> p1, A v1, MetadataProperty<? super B> p2, B v2, MetadataProperty<? super C> p3, C v3, MetadataProperty<? super D> p4, D v4, MetadataProperty<? super E> p5, E v5, MetadataProperty<? super F> p6, F v6, MetadataProperty<? super G> p7, G v7, MetadataProperty<? super H> p8, H v8) {
		return p8.toMeta(v8, p7.toMeta(v7, p6.toMeta(v6, p5.toMeta(v5, p4.toMeta(v4, p3.toMeta(v3, p2.toMeta(v2, p1.toMeta(v1, 0))))))));
	}

	// going beyond 8 is madness, for anything more use the MetaBuilder

	/**
	 * @deprecated use {@link #newProperty(int, Class)} for Enum properties
	 */
	@Deprecated
	@SafeVarargs
	public static <T extends Enum<T>> MetadataProperty<T> newProperty(int shift, T... values) {
		throw new UnsupportedOperationException();
	}

	private static int checkShift(int shift) {
		checkArgument(shift >= 0 && shift <= Ints.BYTES * 8 - 1, "shift must be between 0 and 3 inclusive!");
		return shift;
	}

}
