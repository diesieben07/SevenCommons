package de.take_weiland.mods.commons.meta;

import com.google.common.primitives.Ints;
import de.take_weiland.mods.commons.internal.SCMetaInternalProxy;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.world.World;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public final class MetaProperties {

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

	private static MetaBuilderImpl clientBuilder;
	private static MetaBuilderImpl serverBuilder;

	public static MetaBuilder builder(World world) {
		MetaBuilderImpl b;
		if (world.isRemote) {
			b = clientBuilder;
			if (b == null) {
				return (clientBuilder = new MetaBuilderImpl());
			}
		} else {
			b = serverBuilder;
			if (b == null) {
				return (serverBuilder = new MetaBuilderImpl());
			}
		}
		b.reset();
		return b;
	}

	static {
		SevenCommons.metaProxy = new SCMetaInternalProxy() {
			@Override
			public <E> E[] backingValues(MetadataProperty<E> property) {
				return ((AbstractArrayProperty<E>) property).values();
			}
		};
	}

	public static MetaBuilder builder() {
		return new MetaBuilderImpl();
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

	// going beyond 8 is madness, anything else needs to be handled by the caller

	/**
	 *
	 * @deprecated use {@link #newProperty(int, Class)} for Enum properties
	 */
	@Deprecated
	@SafeVarargs
	public static <T extends Enum<T>> MetadataProperty<T> newProperty(int shift, T... values) {
		checkArgument(checkNotNull(values, "values").length >= 1, "Cannot use Empty enum!");
		return newProperty(shift, values[0].getDeclaringClass());
	}

	private static int checkShift(int shift) {
		checkArgument(shift >= 0 && shift <= Ints.BYTES * 8 - 1, "shift must be between 0 and 3 inclusive!");
		return shift;
	}

}
