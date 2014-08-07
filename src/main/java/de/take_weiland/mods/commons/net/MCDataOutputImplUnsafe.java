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
	void writeBooleans00(boolean[] booleans, int off, int len) {
		int idx = off;
		byte[] buf = this.buf;
		int count = this.count;
		// write 8 booleans per byte
		// as long as we still have at least 8 elements left
		while (len - idx >= 8) {
			long l = unsafe.getLong(booleans, BOOLEAN_BASE_OFF + (idx));
			buf[count++] = (byte) ((l & 0x01)
					| ((l & 0x0100L) >>> 7)
					| ((l & 0x010000L) >>> 14)
					| ((l & 0x01000000L) >>> 21)
					| ((l & 0x0100000000L) >>> 28)
					| ((l & 0x010000000000L) >>> 35)
					| ((l & 0x01000000000000L) >>> 42)
					| ((l & 0x0100000000000000L) >>> 49));
			idx += 8;
		}
		// write any leftover elements in the array
		if ((idx - off) != len) {
			buf[count] = (byte) ((booleans[idx] ? 0b0000_0001 : 0)
					| (idx + 1 < len && booleans[idx + 1] ? 0b0000_0010 : 0)
					| (idx + 2 < len && booleans[idx + 2] ? 0b0000_0100 : 0)
					| (idx + 3 < len && booleans[idx + 3] ? 0b0000_1000 : 0)
					| (idx + 4 < len && booleans[idx + 4] ? 0b0001_0000 : 0)
					| (idx + 5 < len && booleans[idx + 5] ? 0b0010_0000 : 0)
					| (idx + 6 < len && booleans[idx + 6] ? 0b0100_0000 : 0)
					| (idx + 7 < len && booleans[idx + 7] ? 0b1000_0000 : 0));
		}
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
