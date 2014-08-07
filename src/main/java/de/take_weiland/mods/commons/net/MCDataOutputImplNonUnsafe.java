package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
class MCDataOutputImplNonUnsafe extends MCDataOutputImpl {

	MCDataOutputImplNonUnsafe(int initialCap) {
		super(initialCap);
	}

	@Override
	void writeShorts00(short[] shorts, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int i = off; i < end; ++i) {
			short s = shorts[i];
			buf[count++] = (byte) s;
			buf[count++] = (byte) (s >> 8);
		}
	}

	@Override
	void writeInts00(int[] ints, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int idx = off; idx < end; ++idx) {
			int i = ints[idx];
			buf[count++] = (byte) i;
			buf[count++] = (byte) (i >> 8);
			buf[count++] = (byte) (i >> 16);
			buf[count++] = (byte) (i >> 24);
		}
	}

	@Override
	void writeLongs00(long[] longs, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int idx = off; idx < end; ++idx) {
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
	}

	@Override
	void writeChars00(char[] chars, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int i = off; i < end; ++i) {
			char s = chars[i];
			buf[count++] = (byte) s;
			buf[count++] = (byte) (s >> 8);
		}
	}

	@Override
	void writeFloats00(float[] floats, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int idx = off; idx < end; ++idx) {
			int i = Float.floatToRawIntBits(floats[idx]);
			buf[count++] = (byte) i;
			buf[count++] = (byte) (i >> 8);
			buf[count++] = (byte) (i >> 16);
			buf[count++] = (byte) (i >> 24);
		}
	}

	@Override
	void writeDoubles00(double[] doubles, int off, int len) {
		byte[] buf = this.buf;
		int count = this.count;
		int end = off + len;

		for (int idx = off; idx < end; ++idx) {
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
	}
}
