package de.take_weiland.mods.commons.net;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author diesieben07
 */
class FastBAISNonUnsafe extends FastBAIS {

	public FastBAISNonUnsafe(byte[] buf, int off, int len) {
		super(buf, off, len);
	}

	@Override
	public short[] readShorts(short[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 1;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new short[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = (short) ((buf[pos++] & 0xFF) | (buf[pos++] & 0xFF) << 8);
			}
			this.pos = pos;
			return b;
		}
	}

	@Override
	public int[] readInts(int[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 2;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new int[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = (buf[pos++] & 0xFF) | (buf[pos++] & 0xFF) << 8 | (buf[pos++] & 0xFF) << 16 | (buf[pos++] & 0xFF) << 24;
			}
			this.pos = pos;
			return b;
		}
	}

	@Override
	public long[] readLongs(long[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 3;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new long[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = ((long) (buf[pos++] & 0xFF))
						| ((long) (buf[pos++] & 0xFF)) << 8
						| ((long) (buf[pos++] & 0xFF)) << 16
						| ((long) (buf[pos++] & 0xFF)) << 24
						| ((long) (buf[pos++] & 0xFF)) << 32
						| ((long) (buf[pos++] & 0xFF)) << 40
						| ((long) (buf[pos++] & 0xFF)) << 48
						| ((long) (buf[pos++] & 0xFF)) << 56;

			}
			this.pos = pos;
			return b;
		}
	}

	@Override
	public char[] readChars(char[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 1;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new char[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = (char) ((buf[pos++] & 0xFF) | (buf[pos++] & 0xFF) << 8);
			}
			this.pos = pos;
			return b;
		}
	}

	@Override
	public float[] readFloats(float[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 2;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new float[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = Float.intBitsToFloat((buf[pos++] & 0xFF) | (buf[pos++] & 0xFF) << 8 | (buf[pos++] & 0xFF) << 16 | (buf[pos++] & 0xFF) << 24);
			}
			this.pos = pos;
			return b;
		}
	}

	@Override
	public double[] readDoubles(double[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			int byteLen = len << 3;
			checkAvailable(byteLen);
			if (b == null || b.length < len) {
				b = new double[len];
			}
			byte[] buf = this.buf;
			int pos = this.pos;
			for (int i = 0; i < len; ++i) {
				b[i] = Double.longBitsToDouble(((long) (buf[pos++] & 0xFF))
						| ((long) (buf[pos++] & 0xFF)) << 8
						| ((long) (buf[pos++] & 0xFF)) << 16
						| ((long) (buf[pos++] & 0xFF)) << 24
						| ((long) (buf[pos++] & 0xFF)) << 32
						| ((long) (buf[pos++] & 0xFF)) << 40
						| ((long) (buf[pos++] & 0xFF)) << 48
						| ((long) (buf[pos++] & 0xFF)) << 56);

			}
			this.pos = pos;
			return b;
		}
	}
}
