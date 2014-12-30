package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.reflect.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkArgument;

/**
* @author diesieben07
*/
final class BufferUtils {

	static final int ITEM_NULL_ID = 32000;
	static final int BLOCK_NULL_ID = 4096;

	static final boolean useUnsafe = JavaUtils.hasUnsafe() && UnsafeChecks.checkUseable();

	private static final class UnsafeChecks {

		// factored out to avoid loading sun.misc.Unsafe class if it's not available
		static boolean checkUseable() {
			Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();
			// sanity checks to see if the native memory layout allows us to use the fast array copying
			if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN
					|| unsafe.arrayIndexScale(boolean[].class) != 1
					|| unsafe.arrayIndexScale(byte[].class) != 1
					|| unsafe.arrayIndexScale(short[].class) != 2
					|| unsafe.arrayIndexScale(int[].class) != 4
					|| unsafe.arrayIndexScale(long[].class) != 8
					|| unsafe.arrayIndexScale(float[].class) != 4
					|| unsafe.arrayIndexScale(double[].class) != 8) {
				return false;
			}

			long bits0 = 0b0000_0001_0000_0000_0000_0001L;
			boolean[] b = new boolean[8];
			unsafe.putLong(b, (long) unsafe.arrayBaseOffset(boolean[].class), bits0);
			if (!b[0] || b[1] || !b[2]) {
				return false;
			}

			long bits = Double.doubleToRawLongBits(3.4);
			double[] d = new double[1];
			unsafe.putLong(d, (long) unsafe.arrayBaseOffset(double[].class), bits);
			if (d[0] != 3.4) {
				return false;
			}

			int bits2 = Float.floatToRawIntBits(3.4f);
			float[] f = new float[1];
			unsafe.putInt(f, (long) unsafe.arrayBaseOffset(float[].class), bits2);
			return f[0] == 3.4f;
		}

		private UnsafeChecks() { }
	}

	static final BitSetHandler bitSetHandler;
	static final EnumSetHandler enumSetHandler;

	static {
		BitSetHandler bsHandler;
		EnumSetHandler esHandler;
		try {
			BitSet.class.getDeclaredConstructor(long[].class);
			if (BitSet.class.getDeclaredField("wordsInUse").getType() != int.class) {
				throw new RuntimeException("BitSet.wordsInUse not found.");
			}
			if (BitSet.class.getDeclaredField("words").getType() != long[].class) {
				throw new RuntimeException("BitSet.words not found");
			}

			bsHandler = (BitSetHandler) Class.forName("de.take_weiland.mods.commons.net.BufferUtils$BitSetHandlerFast").newInstance();
		} catch (Exception e) {
			bsHandler = new BitSetHandlerPureJava();
		}
		try {
			Class<?> regularEnumSet = Class.forName("java.util.RegularEnumSet");
			if (regularEnumSet.getDeclaredField("elements").getType() != long.class) {
				throw new RuntimeException("RegularEnumSet.elements not of type long");
			}
			esHandler = (EnumSetHandler) Class.forName("de.take_weiland.mods.commons.net.BufferUtils$EnumSetHandlerFast").newInstance();
		} catch (Exception e) {
			esHandler = new EnumSetHandlerPureJava();
		}
		bitSetHandler = bsHandler;
		enumSetHandler = esHandler;
	}

	abstract static class BitSetHandler {

		abstract BitSet createShared(long[] longs);

		abstract void updateInPlace(long[] longs, BitSet bitSet);

		abstract void writeTo(BitSet bitSet, MCDataOutputStream stream);

	}

	private static class BitSetHandlerPureJava extends BitSetHandler {

		@Override
		BitSet createShared(long[] longs) {
			return BitSet.valueOf(longs);
		}

		@Override
		void updateInPlace(long[] longs, BitSet bitSet) {
			BitSet bs = BitSet.valueOf(longs);
			bitSet.clear();
			bitSet.or(bs);
		}

		@Override
		void writeTo(BitSet bitSet, MCDataOutputStream stream) {
			stream.writeLongs(bitSet.toLongArray());
		}
	}

	private static class BitSetHandlerFast extends BitSetHandler {

		@Override
		BitSet createShared(long[] longs) {
			return BitSetAccessor.instance.createBitsetShared(longs);
		}

		@Override
		void updateInPlace(long[] longs, BitSet bitSet) {
			BitSetAccessor.instance.setWords(bitSet, longs);
			BitSetAccessor.instance.setWordsInUse(bitSet, longs.length);
		}

		@Override
		void writeTo(BitSet bitSet, MCDataOutputStream stream) {
			stream.writeLongs(BitSetAccessor.instance.getWords(bitSet), 0, BitSetAccessor.instance.getWordsInUse(bitSet));
		}
	}

	private static interface BitSetAccessor {

		BitSetAccessor instance = SCReflection.createAccessor(BitSetAccessor.class);

		@Getter(field = "wordsInUse")
		int getWordsInUse(BitSet bitSet);

		@Getter(field = "words")
		long[] getWords(BitSet bitSet);

		@Setter(field = "words")
		void setWords(BitSet bitSet, long[] words);

		@Setter(field = "wordsInUse")
		void setWordsInUse(BitSet bitSet, int wordsInUse);

		@Construct
		BitSet createBitsetShared(long[] arr);

	}

	abstract static class EnumSetHandler {

		abstract <E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data);

		abstract <E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long data);

		abstract long asLong(EnumSet<?> set);

	}

	static final class EnumSetHandlerPureJava extends EnumSetHandler {

		@Override
		<E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data) {
			E[] values = JavaUtils.getEnumConstantsShared(clazz);
			int numEnums = values.length;
			checkArgument(numEnums <= 63);
			if (data == MCDataOutputImpl.ENUM_SET_NULL) {
				return null;
			}

			EnumSet<E> set = EnumSet.noneOf(clazz);
			int limit = Math.min(numEnums, 64);
			for (int i = 0; i < limit; i++) {
				if ((data & (1 << i)) != 0) {
					set.add(values[i]);
				}
			}
			return set;
		}

		@Override
		long asLong(EnumSet<?> set) {
			if (set.isEmpty()) {
				return 0;
			}
			long l = 0;
			for (Enum<?> e : set) {
				l |= 1 << e.ordinal();
			}
			return l;
		}

		@Override
		<E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long l) {
			E[] universe = JavaUtils.getEnumConstantsShared(clazz);
			int len = universe.length;
			checkArgument(len <= 63);

			if (l == MCDataOutputImpl.ENUM_SET_NULL) {
				return null;
			} else {
				if (set == null) {
					set = EnumSet.noneOf(clazz);
				} else {
					set.clear();
				}
				for (int i = 0; i < len; i++) {
					if ((l & (1 << i)) != 0) {
						set.add(universe[i]);
					}
				}
				return set;
			}
		}
	}

	static final class EnumSetHandlerFast extends EnumSetHandler {

		@Override
		<E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data) {
			E[] universe = JavaUtils.getEnumConstantsShared(clazz);
			checkArgument(universe.length <= 63);
			if (data == MCDataOutputImpl.ENUM_SET_NULL) {
				return null;
			}

			EnumSet<E> set = EnumSetAcc.instance.newSmallES(clazz, universe);
			EnumSetAcc.instance.setData(set, data);
			return set;
		}

		@Override
		long asLong(EnumSet<?> set) {
			return EnumSetAcc.instance.getData(set);
		}

		@Override
		<E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long data) {
			E[] universe = JavaUtils.getEnumConstantsShared(clazz);
			int len = universe.length;
			checkArgument(len <= 63);

			if (data == MCDataOutputImpl.ENUM_SET_NULL) {
				return null;
			} else {
				if (set == null) {
					set = EnumSetAcc.instance.newSmallES(clazz, universe);
				}
				EnumSetAcc.instance.setData(set, data);
				return set;
			}
		}
	}

	private interface EnumSetAcc {

		EnumSetAcc instance = SCReflection.createAccessor(EnumSetAcc.class);

		@Getter(field = "elements")
		@OverrideTarget("java.util.RegularEnumSet")
		long getData(EnumSet<?> set);

		@Setter(field = "elements")
		@OverrideTarget("java.util.RegularEnumSet")
		void setData(EnumSet<?> set, long data);

		@Construct
		@OverrideTarget("java.util.RegularEnumSet")
		<E extends Enum<E>> EnumSet<E> newSmallES(Class<E> clazz, E[] universe);

	}

	private BufferUtils() { }
}
