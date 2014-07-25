package de.take_weiland.mods.commons.net;

import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
class FastBAOSNonUnsafe extends FastBAOS {

	FastBAOSNonUnsafe() {
		super();
	}

	FastBAOSNonUnsafe(int initialCap) {
		super(initialCap);
	}

	@Override
	public void writeShorts(short[] shorts, int off, int len) {
		checkPositionIndexes(off, off + len, shorts.length);

		writeVarInt(len);

		int byteLen = len << 1;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int i = 0; i < len; ++i) {
			short s = shorts[i];
			buf[count++] = (byte) s;
			buf[count++] = (byte) (s >> 8);
		}
		this.count = count;
	}

	@Override
	public void writeInts(int[] ints, int off, int len) {
		checkPositionIndexes(off, off + len, ints.length);

		writeVarInt(len);

		int byteLen = len << 2;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int idx = 0; idx < len; ++idx) {
			int i = ints[idx];
			buf[count++] = (byte) i;
			buf[count++] = (byte) (i >> 8);
			buf[count++] = (byte) (i >> 16);
			buf[count++] = (byte) (i >> 24);
		}
		this.count = count;
	}

	@Override
	public void writeLongs(long[] longs, int off, int len) {
		checkPositionIndexes(off, off + len, longs.length);

		writeVarInt(len);

		int byteLen = len << 3;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int idx = 0; idx < len; ++idx) {
			long l = longs[idx];
			buf[count++] = (byte) l;
			buf[count++] = (byte) (l >> 8);
			buf[count++] = (byte) (l >> 16);
			buf[count++] = (byte) (l >> 24);
			buf[count++] = (byte) (l >> 32);
			buf[count++] = (byte) (l >> 40);
			buf[count++] = (byte) (l >> 48);
			buf[count++] = (byte) (l >> 56);
		}
		this.count = count;
	}

	@Override
	public void writeChars(char[] chars, int off, int len) {
		checkPositionIndexes(off, off + len, chars.length);

		writeVarInt(len);

		int byteLen = len << 1;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int i = 0; i < len; ++i) {
			char s = chars[i];
			buf[count++] = (byte) s;
			buf[count++] = (byte) (s >> 8);
		}
		this.count = count;
	}

	@Override
	public void writeFloats(float[] floats, int off, int len) {
		checkPositionIndexes(off, off + len, floats.length);

		writeVarInt(len);

		int byteLen = len << 2;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int idx = 0; idx < len; ++idx) {
			int i = Float.floatToRawIntBits(floats[idx]);
			buf[count++] = (byte) i;
			buf[count++] = (byte) (i >> 8);
			buf[count++] = (byte) (i >> 16);
			buf[count++] = (byte) (i >> 24);
		}
		this.count = count;
	}

	@Override
	public void writeDoubles(double[] doubles, int off, int len) {
		checkPositionIndexes(off, off + len, doubles.length);

		writeVarInt(len);

		int byteLen = len << 3;
		ensureCapacity(byteLen);

		byte[] buf = this.buf;
		int count = this.count;

		for (int idx = 0; idx < len; ++idx) {
			long l = Double.doubleToRawLongBits(doubles[idx]);
			buf[count++] = (byte) l;
			buf[count++] = (byte) (l >> 8);
			buf[count++] = (byte) (l >> 16);
			buf[count++] = (byte) (l >> 24);
			buf[count++] = (byte) (l >> 32);
			buf[count++] = (byte) (l >> 40);
			buf[count++] = (byte) (l >> 48);
			buf[count++] = (byte) (l >> 56);
		}
		this.count = count;
	}
}
