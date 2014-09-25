package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.reflect.Construct;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.reflect.Setter;
import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

import java.nio.ByteOrder;
import java.util.BitSet;

/**
* @author diesieben07
*/
final class BufferUtils {

	static final int ITEM_NULL_ID = 32000;
	static final int BLOCK_NULL_ID = 4096;

	static boolean canUseUnsafe() {
		return UnsafeChecks.checkUseable();
	}

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

	static {
		BitSetHandler handler;
		try {
			BitSet.class.getDeclaredConstructor(long[].class);
			if (BitSet.class.getDeclaredField("wordsInUse").getType() != int.class) {
				throw new RuntimeException("BitSet.wordsInUse not found.");
			}
			if (BitSet.class.getDeclaredField("words").getType() != long[].class) {
				throw new RuntimeException("BitSet.words not found");
			}

			handler = (BitSetHandler) Class.forName("de.take_weiland.mods.commons.net.BufferUtils$BitSetHandlerFast").newInstance();
		} catch (Exception e) {
			handler = new BitSetHandlerPureJava();
		}
		bitSetHandler = handler;
	}

	abstract static class BitSetHandler {

		abstract BitSet createShared(long[] longs);

		abstract BitSet updateInPlace(long[] longs, BitSet bitSet);

		abstract void writeTo(BitSet bitSet, MCDataOutputStream stream);

	}

	static class BitSetHandlerPureJava extends BitSetHandler {

		@Override
		BitSet createShared(long[] longs) {
			return BitSet.valueOf(longs);
		}

		@Override
		BitSet updateInPlace(long[] longs, BitSet bitSet) {
			return BitSet.valueOf(longs);
		}

		@Override
		void writeTo(BitSet bitSet, MCDataOutputStream stream) {
			stream.writeLongs(bitSet.toLongArray());
		}
	}

	static class BitSetHandlerFast extends BitSetHandler {

		@Override
		BitSet createShared(long[] longs) {
			return BitSetAccessor.instance.createBitsetShared(longs);
		}

		@Override
		BitSet updateInPlace(long[] longs, BitSet bitSet) {
			BitSetAccessor.instance.setWords(bitSet, longs);
			BitSetAccessor.instance.setWordsInUse(bitSet, longs.length);
			return bitSet;
		}

		@Override
		void writeTo(BitSet bitSet, MCDataOutputStream stream) {
			stream.writeLongs(BitSetAccessor.instance.getWords(bitSet), 0, BitSetAccessor.instance.getWordsInUse(bitSet));
		}
	}

	static interface BitSetAccessor {

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

	private BufferUtils() { }
}
