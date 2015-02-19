package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.asm.MCPNames;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class ArrayConversions {

    private ArrayConversions() { }

    static MethodHandle getReader(Class<?> arrClass) {
        Class<?> repr;
        MethodHandle read;
        if (arrClass == boolean[].class || arrClass == short[].class || arrClass == char[].class) {
            repr = byte[].class;
            read = readByteArr();
        } else {
            repr = int[].class;
            read = readIntArr();
        }

        if (repr != arrClass) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle conv;
            try {
                conv = lookup.findStatic(ArrayConversions.class, "d" + arrClass.getComponentType().getName(), methodType(arrClass, repr));
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
            read = MethodHandles.filterReturnValue(read, conv);
        }
        return read;
    }

    static MethodHandle getWriter(Class<?> arrClass) {
        Class<?> repr;
        MethodHandle write;
        if (arrClass == boolean[].class || arrClass == short[].class || arrClass == char[].class) {
            repr = byte[].class;
            write = writeByteArr();
        } else {
            repr = int[].class;
            write = writeIntArr();
        }

        if (repr != arrClass) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle conv;
            try {
                conv = lookup.findStatic(ArrayConversions.class, "e" + arrClass.getComponentType().getName(), methodType(repr, arrClass));
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
            write = MethodHandles.filterArguments(write, 0, conv);
        }
        return write;
    }

    private static MethodHandle WR_BYTE_ARR;
    private static MethodHandle RE_BYTE_ARR;
    private static MethodHandle WR_INT_ARR;
    private static MethodHandle RE_INT_ARR;

    private static MethodHandle writeByteArr() {
        if (WR_BYTE_ARR == null) {
            try {
                WR_BYTE_ARR = MethodHandles.publicLookup()
                        .findConstructor(NBTTagByteArray.class, methodType(void.class, String.class, byte[].class))
                        .bindTo("")
                        .asType(methodType(NBTBase.class, byte[].class));
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
        }
        return WR_BYTE_ARR;
    }

    private static MethodHandle readByteArr() {
        if (RE_BYTE_ARR == null) {
            try {
                RE_BYTE_ARR = MethodHandles.publicLookup()
                        .findGetter(NBTTagByteArray.class, MCPNames.field(MCPNames.F_NBT_BYTE_ARR_DATA), byte[].class);
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
        }
        return RE_BYTE_ARR;
    }

    private static MethodHandle writeIntArr() {
        if (WR_INT_ARR == null) {
            try {
                WR_INT_ARR = MethodHandles.publicLookup()
                        .findConstructor(NBTTagIntArray.class, methodType(void.class, String.class, int[].class))
                        .bindTo("")
                        .asType(methodType(NBTBase.class, int[].class));
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
        }
        return WR_INT_ARR;
    }

    private static MethodHandle readIntArr() {
        if (RE_INT_ARR == null) {
            try {
                RE_INT_ARR = MethodHandles.publicLookup()
                        .findGetter(NBTTagIntArray.class, MCPNames.field(MCPNames.F_NBT_INT_ARR_DATA), int[].class);
            } catch (ReflectiveOperationException e) {
                throw Throwables.propagate(e);
            }
        }
        return RE_INT_ARR;
    }

    static byte[] eS(short[] value) {
        int sLen = value.length;
        byte[] bytes = new byte[sLen << 1];
        for (int sOff = 0, bOff = 0; sOff < sLen; sOff++) {
            short s = value[sOff];
            bytes[bOff++] = (byte) s;
            bytes[bOff++] = (byte) (s >>> 8);
        }
        return bytes;
    }

    static short[] dS(byte[] bytes) {
        int sLen = bytes.length << 1;
        short[] shorts = new short[sLen];
        for (int sOff = 0, boff = 0; sOff < sLen; sOff++) {
            shorts[sOff] = (short) (bytes[boff++] | bytes[boff++] << 8);
        }
        return shorts;
    }

    static byte[] eC(char[] value) {
        int cLen = value.length;
        byte[] bytes = new byte[cLen << 1];
        for (int sOff = 0, bOff = 0; sOff < cLen; sOff++) {
            char c = value[sOff];
            bytes[bOff++] = (byte) c;
            bytes[bOff++] = (byte) (c >>> 8);
        }
        return bytes;
    }

    static char[] dC(byte[] bytes) {
        int cLen = bytes.length >>> 1;
        char[] chars = new char[cLen];
        for (int cOff = 0, boff = 0; cOff < cLen; cOff++) {
            chars[cOff] = (char) (bytes[boff++] | bytes[boff++] << 8);
        }
        return chars;
    }

    static int[] eJ(long[] value) {
        int lLen = value.length;
        int[] ints = new int[lLen << 1];
        for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
            long l = value[lOff];
            ints[iOff++] = (int) l;
            ints[iOff++] = (int) (l >>> 32);
        }
        return ints;
    }

    static long[] dJ(int[] ints) {
        int lLen = ints.length << 1;
        long[] longs = new long[lLen];
        for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
            longs[lOff] = (long) ints[iOff++] | (long) ints[iOff++] << 32;
        }
        return longs;
    }

    static int[] eF(float[] value) {
        int len = value.length;
        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = Float.floatToRawIntBits(value[i]);
        }
        return ints;
    }

    static float[] dF(int[] ints) {
        int len = ints.length;
        float[] floats = new float[len];
        for (int i = 0; i < len; i++) {
            floats[i] = Float.intBitsToFloat(ints[i]);
        }
        return floats;
    }

    static int[] eD(double[] value) {
        int dLen = value.length;
        int[] ints = new int[dLen << 1];
        for (int lOff = 0, iOff = 0; lOff < dLen; ++lOff) {
            long l = Double.doubleToLongBits(value[lOff]);
            ints[iOff++] = (int) l;
            ints[iOff++] = (int) (l >>> 32);
        }
        return ints;
    }

    static double[] dD(int[] ints) {
        int dLen = ints.length << 1;
        double[] doubles = new double[dLen];
        for (int dOff = 0, iOff = 0; dOff < dLen; ++dOff) {
            doubles[dOff] = Double.longBitsToDouble((long) ints[iOff++] | (long) ints[iOff++] << 32);
        }
        return doubles;
    }

    static byte[] eZ(boolean[] value) {
        int n = value.length;
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            if (value[i]) bytes[i] = 1;
        }
        return bytes;
    }

    static boolean[] dZ(byte[] bytes) {
        int n = bytes.length;
        boolean[] booleans = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (bytes[i] != 0) booleans[i] = true;
        }
        return booleans;
    }

}
