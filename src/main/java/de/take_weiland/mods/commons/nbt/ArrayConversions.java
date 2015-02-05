package de.take_weiland.mods.commons.nbt;

/**
 * @author diesieben07
 */
final class ArrayConversions {

	private ArrayConversions() { }

	static byte[] encodeInts(int[] value) {
		int iLen = value.length;
		byte[] bytes = new byte[iLen << 2];
		for (int iOff = 0, bOff = 0; iOff < iLen; iOff++) {
			int i = value[iOff];
			bytes[bOff] = (byte) (i & 0xff);
			bytes[bOff + 1] = (byte) ((i >> 8) & 0xff);
			bytes[bOff + 2] = (byte) ((i >> 16) & 0xff);
			bytes[bOff + 3] = (byte) ((i >> 24) & 0xff);

			bOff += 4;
		}
		return bytes;
	}

	static int[] decodeInts(byte[] bytes) {
		int iLen = bytes.length << 2;
		int[] ints = new int[iLen];
		for (int i = 0, bOff = 0; i < iLen; ++i) {
			ints[i] = (int) bytes[bOff]
					| (int) bytes[bOff + 1] << 8
					| (int) bytes[bOff + 2] << 16
					| (int) bytes[bOff + 3] << 24;
			bOff += 4;
		}
		return ints;
	}

	static byte[] encodeShorts(short[] value) {
		int sLen = value.length;
		byte[] bytes = new byte[sLen << 1];
		for (int sOff = 0, bOff = 0; sOff < sLen; sOff++) {
			short s = value[sOff];
			bytes[bOff] = (byte) (s & 0xff);
			bytes[bOff + 1] = (byte) ((s >> 8) & 0xff);
			bOff += 2;
		}
		return bytes;
	}

	static short[] decodeShorts(byte[] bytes) {
		int sLen = bytes.length << 1;
		short[] shorts = new short[sLen];
		for (int sOff = 0, boff = 0; sOff < sLen; sOff++) {
			shorts[sOff] = (short) (bytes[boff] | bytes[boff + 1] << 8);
			boff += 2;
		}
		return shorts;
	}

	static byte[] encodeChars(char[] value) {
		int cLen = value.length;
		byte[] bytes = new byte[cLen << 1];
		for (int sOff = 0, bOff = 0; sOff < cLen; sOff++) {
			char c = value[sOff];
			bytes[bOff] = (byte) (c & 0xff);
			bytes[bOff + 1] = (byte) ((c >> 8) & 0xff);
			bOff += 2;
		}
		return bytes;
	}

	static char[] decodeChars(byte[] bytes) {
		int cLen = bytes.length << 1;
		char[] chars = new char[cLen];
		for (int cOff = 0, boff = 0; cOff < cLen; cOff++) {
			chars[cOff] = (char) (bytes[boff] | bytes[boff + 1] << 8);
			boff += 2;
		}
		return chars;
	}

	static int[] encodeLongs(long[] value) {
		int lLen = value.length;
		int[] ints = new int[lLen << 1];
		for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
			long l = value[lOff];
			ints[iOff] = (int) (l);
			ints[iOff + 1] = (int) ((l >> 32));

			iOff += 2;
		}
		return ints;
	}

	static long[] decodeLongs(int[] ints) {
		int lLen = ints.length << 1;
		long[] longs = new long[lLen];
		for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
			longs[lOff] = (long) ints[iOff] | (long) ints[iOff + 1] << 32;
			iOff += 2;
		}
		return longs;
	}

	static int[] encodeFloats(float[] value) {
		int len = value.length;
		int[] ints = new int[len];
		for (int i = 0; i < len; i++) {
			ints[i] = Float.floatToIntBits(value[i]);
		}
		return ints;
	}

	static float[] decodeFloats(int[] ints) {
		int len = ints.length;
		float[] floats = new float[len];
		for (int i = 0; i < len; i++) {
			floats[i] = Float.intBitsToFloat(ints[i]);
		}
		return floats;
	}

	static int[] encodeDoubles(double[] value) {
		int dLen = value.length;
		int[] ints = new int[dLen << 1];
		for (int lOff = 0, iOff = 0; lOff < dLen; ++lOff) {
			long l = Double.doubleToLongBits(value[lOff]);
			ints[iOff] = (int) l;
			ints[iOff + 1] = (int) (l >> 32);

			iOff += 2;
		}
		return ints;
	}

	static double[] decodeDoubles(int[] ints) {
		int dLen = ints.length << 1;
		double[] doubles = new double[dLen];
		for (int dOff = 0, iOff = 0; dOff < dLen; ++dOff) {
			doubles[dOff] = Double.longBitsToDouble((long) ints[iOff] | (long) ints[iOff + 1] << 32);
			iOff += 2;
		}
		return doubles;
	}

	static byte[] encodeBooleans(boolean[] value) {
		int boolLen = value.length;
		int fullBytes = boolLen & ~7; // boolLen - (boolLen % 8)
		int byteLen = (boolLen >> 3) + (((boolLen) | (boolLen >> 1)) & 1); // boolLen / 8 + (boolLen % 8 == 0 ? 0 : 1)
		byte[] bytes = new byte[byteLen];

		int byOff = 0;
		int boOff;
		for (boOff = 0; boOff < fullBytes; boOff += 8) {
			bytes[byOff++] = (byte) ((value[boOff] ? 1 : 0)
					| (value[boOff + 1] ? 2 : 0)
					| (value[boOff + 2] ? 4 : 0)
					| (value[boOff + 3] ? 8 : 0)
					| (value[boOff + 4] ? 16 : 0)
					| (value[boOff + 5] ? 32 : 0)
					| (value[boOff + 6] ? 64 : 0)
					| (value[boOff + 7] ? 128 : 0));
		}
		if (boolLen != fullBytes) {
			byte val = 0;
			int idx = 0;
			for (; boOff < boolLen; boOff++) {
				val |= 1 << idx++;
			}
			bytes[byteLen - 1] = val;
		}
		return bytes;
	}

	static boolean[] decodeBooleans(byte[] bytes, int boLen) {
		int byLen = bytes.length;
		int fullBytes = boLen >> 3; // = boLen / 8

		boolean[] booleans = new boolean[boLen];
		int boIdx = 0;
		for (int byIdx = 0; byIdx < fullBytes; byIdx++) {
			byte val = bytes[byIdx];

			booleans[boIdx] = (val & 1) != 0;
			booleans[boIdx + 1] = (val & 2) != 0;
			booleans[boIdx + 2] = (val & 4) != 0;
			booleans[boIdx + 3] = (val & 8) != 0;
			booleans[boIdx + 4] = (val & 16) != 0;
			booleans[boIdx + 5] = (val & 32) != 0;
			booleans[boIdx + 6] = (val & 64) != 0;
			booleans[boIdx + 7] = (val & 128) != 0;

			boIdx += 8;
		}

		if (byLen != fullBytes) {
			byte val = bytes[byLen - 1];
			int idx = 0;
			do {
				booleans[boIdx++] = (val & (1 << idx++)) != 0;
			} while (boIdx < boLen);
		}

		return booleans;
	}

}
