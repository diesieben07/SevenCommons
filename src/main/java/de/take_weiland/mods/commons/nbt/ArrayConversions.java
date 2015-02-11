package de.take_weiland.mods.commons.nbt;

/**
 * @author diesieben07
 */
final class ArrayConversions {

    private ArrayConversions() {
    }

    static byte[] encodeShorts(short[] value) {
        int sLen = value.length;
        byte[] bytes = new byte[sLen << 1];
        for (int sOff = 0, bOff = 0; sOff < sLen; sOff++) {
            short s = value[sOff];
            bytes[bOff++] = (byte) s;
            bytes[bOff++] = (byte) (s >>> 8);
        }
        return bytes;
    }

    static short[] decodeShorts(byte[] bytes) {
        int sLen = bytes.length << 1;
        short[] shorts = new short[sLen];
        for (int sOff = 0, boff = 0; sOff < sLen; sOff++) {
            shorts[sOff] = (short) (bytes[boff++] | bytes[boff++] << 8);
        }
        return shorts;
    }

    static byte[] encodeChars(char[] value) {
        int cLen = value.length;
        byte[] bytes = new byte[cLen << 1];
        for (int sOff = 0, bOff = 0; sOff < cLen; sOff++) {
            char c = value[sOff];
            bytes[bOff++] = (byte) c;
            bytes[bOff++] = (byte) (c >>> 8);
        }
        return bytes;
    }

    static char[] decodeChars(byte[] bytes) {
        int cLen = bytes.length >>> 1;
        char[] chars = new char[cLen];
        for (int cOff = 0, boff = 0; cOff < cLen; cOff++) {
            chars[cOff] = (char) (bytes[boff++] | bytes[boff++] << 8);
        }
        return chars;
    }

    static int[] encodeLongs(long[] value) {
        int lLen = value.length;
        int[] ints = new int[lLen << 1];
        for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
            long l = value[lOff];
            ints[iOff++] = (int) l;
            ints[iOff++] = (int) (l >>> 32);
        }
        return ints;
    }

    static long[] decodeLongs(int[] ints) {
        int lLen = ints.length << 1;
        long[] longs = new long[lLen];
        for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
            longs[lOff] = (long) ints[iOff++] | (long) ints[iOff++] << 32;
        }
        return longs;
    }

    static int[] encodeFloats(float[] value) {
        int len = value.length;
        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = Float.floatToRawIntBits(value[i]);
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
            ints[iOff++] = (int) l;
            ints[iOff++] = (int) (l >>> 32);
        }
        return ints;
    }

    static double[] decodeDoubles(int[] ints) {
        int dLen = ints.length << 1;
        double[] doubles = new double[dLen];
        for (int dOff = 0, iOff = 0; dOff < dLen; ++dOff) {
            doubles[dOff] = Double.longBitsToDouble((long) ints[iOff++] | (long) ints[iOff++] << 32);
        }
        return doubles;
    }

    static byte[] encodeBooleans(boolean[] value) {
        int nBits = value.length;
        int lastByteBits = nBits & 7;
        int nBytes = (nBits >> 3) + (-lastByteBits >>> 31) + 1; // 1 byte for size
        byte[] bytes = new byte[nBytes];

        bytes[0] = (byte) lastByteBits;

        int cByteIdx = 1;
        byte cByte = 0;
        for (int bit = 0; bit < nBits; bit++) {
            int bitInByte = bit & 7;

            if (value[bit]) {
                cByte |= (1 << bitInByte);
            }

            if (bitInByte == 0b111) {
                bytes[cByteIdx++] = cByte;
                cByte = 0;
            }
        }
        if (cByte != 0) {
            bytes[cByteIdx] = cByte;
        }
        return bytes;
    }

    static boolean[] decodeBooleans(byte[] bytes) {
        int nBytes = bytes.length;
        int nBits = ((nBytes - 2) << 3) + bytes[0];
        boolean[] bits = new boolean[nBits];

        int cByteIdx = 1;
        byte cByte = 0;
        for (int bit = 0; bit < nBits; bit++) {
            int bitInByte = bit & 7;
            if (bitInByte == 0) {
                cByte = bytes[cByteIdx++];
            }
            bits[bit] = (cByte & (1 << bitInByte)) != 0;
        }

        return bits;
    }

}
