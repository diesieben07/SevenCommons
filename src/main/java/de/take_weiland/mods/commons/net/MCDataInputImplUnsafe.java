package de.take_weiland.mods.commons.net;


import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.ArrayUtils;
import sun.misc.Unsafe;

/**
 * @author diesieben07
 */
class MCDataInputImplUnsafe extends MCDataInputImpl {

	// arbitrary, could be in the OutputStream as well
	static final Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();
	static final long BOOLEAN_BASE_OFF = unsafe.arrayBaseOffset(boolean[].class);
	static final long BYTE_BASE_OFF = unsafe.arrayBaseOffset(byte[].class);
	static final long SHORT_BASE_OFF = unsafe.arrayBaseOffset(short[].class);
	static final long CHAR_BASE_OFF = unsafe.arrayBaseOffset(char[].class);
	static final long INT_BASE_OFF = unsafe.arrayBaseOffset(int[].class);
	static final long LONG_BASE_OFF = unsafe.arrayBaseOffset(long[].class);
	static final long FLOAT_BASE_OFF = unsafe.arrayBaseOffset(float[].class);
	static final long DOUBLE_BASE_OFF = unsafe.arrayBaseOffset(double[].class);

	public MCDataInputImplUnsafe(byte[] buf, int off, int len) {
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, SHORT_BASE_OFF, byteLen);
			pos += len;
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, INT_BASE_OFF, byteLen);
			pos += len;
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, LONG_BASE_OFF, byteLen);
			pos += len;
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, CHAR_BASE_OFF, byteLen);
			pos += len;
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, FLOAT_BASE_OFF, byteLen);
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
			unsafe.copyMemory(buf, BYTE_BASE_OFF + pos, b, DOUBLE_BASE_OFF, byteLen);
			return b;
		}
	}
}
