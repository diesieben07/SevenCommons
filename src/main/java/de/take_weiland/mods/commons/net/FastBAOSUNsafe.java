package de.take_weiland.mods.commons.net;

import static com.google.common.base.Preconditions.checkPositionIndexes;
import static de.take_weiland.mods.commons.net.FastBAISUnsafe.*;

/**
 * @author diesieben07
 */
class FastBAOSUnsafe extends FastBAOS {

	FastBAOSUnsafe() {
		super();
	}

	FastBAOSUnsafe(int initialCap) {
		super(initialCap);
	}

	@Override
	public void writeShorts(short[] shorts, int off, int len) {
		checkPositionIndexes(off, off + len, shorts.length);
		writeVarInt(len);

		int byteLen = len << 1;
		ensureCapacity(byteLen);

		unsafe.copyMemory(shorts, SHORT_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}

	@Override
	public void writeInts(int[] ints, int off, int len) {
		checkPositionIndexes(off, off + len, ints.length);
		writeVarInt(len);

		int byteLen = len << 2;
		ensureCapacity(byteLen);

		unsafe.copyMemory(ints, INT_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}

	@Override
	public void writeLongs(long[] longs, int off, int len) {
		checkPositionIndexes(off, off + len, longs.length);
		writeVarInt(len);

		int byteLen = len << 3;
		ensureCapacity(byteLen);

		unsafe.copyMemory(longs, LONG_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}

	@Override
	public void writeChars(char[] chars, int off, int len) {
		checkPositionIndexes(off, off + len, chars.length);
		writeVarInt(len);

		int byteLen = len << 1;
		ensureCapacity(byteLen);

		unsafe.copyMemory(chars, CHAR_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}

	@Override
	public void writeFloats(float[] floats, int off, int len) {
		checkPositionIndexes(off, off + len, floats.length);
		writeVarInt(len);

		int byteLen = len << 2;
		ensureCapacity(byteLen);

		unsafe.copyMemory(floats, FLOAT_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}

	@Override
	public void writeDoubles(double[] doubles, int off, int len) {
		checkPositionIndexes(off, off + len, doubles.length);
		writeVarInt(len);

		int byteLen = len << 3;
		ensureCapacity(byteLen);

		unsafe.copyMemory(doubles, DOUBLE_BASE_OFF + byteLen, buf, BYTE_BASE_OFF + count, byteLen);
		count += byteLen;
	}
}
