package de.take_weiland.mods.commons.util;

/**
 * @author diesieben07
 */
public final class ArrayConversions {

	public static byte[] encodeInts(int[] value) {
		int iLen = value.length;
		byte[] bytes = new byte[iLen * 4];
		for (int lOff = 0, bOff = 0; lOff < iLen; ++lOff) {
			int i = value[lOff];
			bytes[bOff] = (byte) (i & 0xff);
			bytes[bOff + 1] = (byte) ((i >> 8) & 0xff);
			bytes[bOff + 2] = (byte) ((i >> 16) & 0xff);
			bytes[bOff + 3] = (byte) ((i >> 24) & 0xff);

			bOff += 4;
		}
		return bytes;
	}

	public static int[] decodeInts(byte[] bytes) {
		int iLen = bytes.length << 2;
		int[] ints = new int[iLen];
		for (int i = 0, bOff = 0; i < iLen; ++i) {
			ints[i] = (int) bytes[bOff]
					| (int) bytes[bOff + 1] << 8
					| (int) bytes[bOff + 2] << 16
					| (int) bytes[bOff + 3] << 24;
			bOff += 8;
		}
		return ints;
	}

	public static byte[] encodeLongs(long[] value) {
		int lLen = value.length;
		byte[] bytes = new byte[lLen * 8];
		for (int lOff = 0, bOff = 0; lOff < lLen; ++lOff) {
			long l = value[lOff];
			bytes[bOff] = (byte) (l & 0xff);
			bytes[bOff + 1] = (byte) ((l >> 8) & 0xff);
			bytes[bOff + 2] = (byte) ((l >> 16) & 0xff);
			bytes[bOff + 3] = (byte) ((l >> 24) & 0xff);
			bytes[bOff + 4] = (byte) ((l >> 32) & 0xff);
			bytes[bOff + 5] = (byte) ((l >> 40) & 0xff);
			bytes[bOff + 6] = (byte) ((l >> 48) & 0xff);
			bytes[bOff + 7] = (byte) ((l >> 56) & 0xff);

			bOff += 8;
		}
		return bytes;
	}

	public static long[] decodeLongs(byte[] bytes) {
		int lLen = bytes.length << 3;
		long[] longs = new long[lLen];
		for (int i = 0, bOff = 0; i < lLen; ++i) {
			longs[i] = (long) bytes[bOff]
					| (long) bytes[bOff + 1] << 8
					| (long) bytes[bOff + 2] << 16
					| (long) bytes[bOff + 3] << 24
					| (long) bytes[bOff + 4] << 32
					| (long) bytes[bOff + 5] << 40
					| (long) bytes[bOff + 6] << 48
					| (long) bytes[bOff + 7] << 56;
			bOff += 8;
		}
		return longs;
	}

	public static byte[] encodeDoubles(double[] value) {
		int dLen = value.length;
		byte[] bytes = new byte[dLen * 8];
		for (int lOff = 0, bOff = 0; lOff < dLen; ++lOff) {
			long l = Double.doubleToLongBits(value[lOff]);
			bytes[bOff] = (byte) (l & 0xff);
			bytes[bOff + 1] = (byte) ((l >> 8) & 0xff);
			bytes[bOff + 2] = (byte) ((l >> 16) & 0xff);
			bytes[bOff + 3] = (byte) ((l >> 24) & 0xff);
			bytes[bOff + 4] = (byte) ((l >> 32) & 0xff);
			bytes[bOff + 5] = (byte) ((l >> 40) & 0xff);
			bytes[bOff + 6] = (byte) ((l >> 48) & 0xff);
			bytes[bOff + 7] = (byte) ((l >> 56) & 0xff);

			bOff += 8;
		}
		return bytes;
	}

	public static double[] decodeDoubles(byte[] bytes) {
		int dLen = bytes.length << 3;
		double[] doubles = new double[dLen];
		for (int i = 0, bOff = 0; i < dLen; ++i) {
			doubles[i] = Double.longBitsToDouble((long) bytes[bOff]
					| (long) bytes[bOff + 1] << 8
					| (long) bytes[bOff + 2] << 16
					| (long) bytes[bOff + 3] << 24
					| (long) bytes[bOff + 4] << 32
					| (long) bytes[bOff + 5] << 40
					| (long) bytes[bOff + 6] << 48
					| (long) bytes[bOff + 7] << 56);
			bOff += 8;
		}
		return doubles;
	}

}
