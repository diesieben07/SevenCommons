package de.take_weiland.mods.commons.net;

import static de.take_weiland.mods.commons.net.MCDataInputImplUnsafe.*;

/**
 * @author diesieben07
 */
class MCDataOutputImplUnsafe extends MCDataOutputImpl {

	MCDataOutputImplUnsafe(int initialCap) {
		super(initialCap);
	}

	@Override
	void writeShorts00(short[] shorts, int off, int len) {
		unsafe.copyMemory(shorts, SHORT_BASE_OFF + (off << 1), buf, BYTE_BASE_OFF + count, len << 1);
	}

	@Override
	void writeInts00(int[] ints, int off, int len) {
		unsafe.copyMemory(ints, INT_BASE_OFF + (off << 2), buf, BYTE_BASE_OFF + count, len << 2);
	}

	@Override
	void writeLongs00(long[] longs, int off, int len) {
		unsafe.copyMemory(longs, LONG_BASE_OFF + (off << 3), buf, BYTE_BASE_OFF + count, len << 3);
	}

	@Override
	void writeChars00(char[] chars, int off, int len) {
		unsafe.copyMemory(chars, CHAR_BASE_OFF + (off << 1), buf, BYTE_BASE_OFF + count, len << 1);
	}

	@Override
	void writeFloats00(float[] floats, int off, int len) {
		unsafe.copyMemory(floats, FLOAT_BASE_OFF + (off << 2), buf, BYTE_BASE_OFF + count, len << 2);
	}

	@Override
	void writeDoubles00(double[] doubles, int off, int len) {
		unsafe.copyMemory(doubles, DOUBLE_BASE_OFF + (off << 3), buf, BYTE_BASE_OFF + count, (len << 3));
	}
}
