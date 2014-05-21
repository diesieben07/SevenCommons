package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.nbt.NBTSerializable;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import net.minecraft.nbt.*;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Array;

/**
 * @author diesieben07
 */
public final class NBTASMHooks {

	public static void putInto(NBTTagCompound into, String key, NBTBase value) {
		into.setTag(key, value);
	}

	public static NBTBase getFrom(NBTTagCompound from, String key) {
		return from.getTag(key);
	}

	public static NBTBase convert(NBTSerializable value) {
		return value.serialize();
	}

	public static NBTBase convert(ByteStreamSerializable value) {
		WritableDataBuf buf = DataBuffers.newWritableBuffer();
		value.write(buf);
		return new NBTTagByteArray("", buf.toByteArray());
	}

	public static NBTBase convert(String value) {
		return new NBTTagString(value);
	}

	public static NBTBase convert(Enum<?> e) {
		return new NBTTagString(e.name());
	}

	public static NBTBase convert(boolean b) {
		return new NBTTagByte("", (byte) (b ? 1 : 0));
	}

	public static NBTBase convert(byte b) {
		return new NBTTagByte("", b);
	}

	public static NBTBase convert(short s) {
		return new NBTTagShort("", s);
	}

	public static NBTBase convert(int i) {
		return new NBTTagInt("", i);
	}

	public static NBTBase convert(long l) {
		return new NBTTagLong("", l);
	}

	public static NBTBase convert(float f) {
		return new NBTTagFloat("", f);
	}

	public static NBTBase convert(double d) {
		return new NBTTagDouble("", d);
	}

	public static NBTBase convert(char c) {
		return new NBTTagShort("", (short) c);
	}

	public static NBTBase convert(boolean[] arr) {
		byte[] bytes = booleanToByte(arr);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("l", arr.length);
		nbt.setByteArray("b", bytes);
		return nbt;
	}

	public static NBTBase convert(byte[] arr) {
		return new NBTTagByteArray("", arr);
	}

	public static NBTBase convert(short[] arr) {
		return new NBTTagByteArray("", shortToByte(arr));
	}

	public static NBTBase convert(int[] arr) {
		return new NBTTagIntArray("", arr);
	}

	public static NBTBase convert(long[] arr) {
		return new NBTTagIntArray("", longToInt(arr));
	}

	public static NBTBase convert(float[] arr) {
		return new NBTTagIntArray("", floatToInt(arr));
	}

	public static NBTBase convert(double[] arr) {
		return new NBTTagIntArray("", doubleToInt(arr));
	}

	public static NBTBase convert(char[] arr) {
		return new NBTTagByteArray("", charToByte(arr));
	}

	public static NBTBase convert(String[] arr) {
		NBTTagList nbt = new NBTTagList();
		for (String s : arr) {
			nbt.appendTag(new NBTTagString("", s));
		}
		return nbt;
	}

	public static NBTBase convert(Enum<?>[] arr) {
		NBTTagList nbt = new NBTTagList();
		for (Enum<?> e : arr) {
			nbt.appendTag(new NBTTagString("", e.name()));
		}
		return nbt;
	}

	public static NBTBase convert_deep_boolean(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (boolean[] subArr : (boolean[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_boolean(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_byte(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (byte[] subArr : (byte[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_byte(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_short(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (short[] subArr : (short[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_short(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_int(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (int[] subArr : (int[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_int(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_long(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (long[] subArr : (long[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_long(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_float(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (float[] subArr : (float[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_float(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_double(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (double[] subArr : (double[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_double(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_char(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (char[] subArr : (char[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_char(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_java_lang_Enum(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (Enum<?>[] subArr : (Enum<?>[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_java_lang_Enum(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static NBTBase convert_deep_java_lang_String(Object[] arr, int dimensions) {
		assert(dimensions >= 2);
		NBTTagList nbt = new NBTTagList();
		if (dimensions == 2) {
			for (String[] subArr : (String[][]) arr) {
				nbt.appendTag(convert(subArr));
			}
		} else {
			for (Object[] subArr : (Object[][]) arr) {
				nbt.appendTag(convert_deep_java_lang_String(subArr, dimensions - 1));
			}
		}
		return nbt;
	}

	public static boolean get_boolean(NBTBase nbt) {
		return nbt != null && ((NBTTagByte) nbt).data != 0;
	}

	public static byte get_byte(NBTBase nbt) {
		return nbt == null ? 0 : ((NBTTagByte) nbt).data;
	}

	public static short get_short(NBTBase nbt) {
		return nbt == null ? 0 : ((NBTTagShort) nbt).data;
	}

	public static int get_int(NBTBase nbt) {
		return nbt == null ? 0 :((NBTTagInt) nbt).data;
	}

	public static long get_long(NBTBase nbt) {
		return nbt == null ? 0 : ((NBTTagLong) nbt).data;
	}

	public static float get_float(NBTBase nbt) {
		return nbt == null ? 0 : ((NBTTagFloat) nbt).data;
	}

	public static double get_double(NBTBase nbt) {
		return nbt == null ? 0 : ((NBTTagDouble) nbt).data;
	}

	public static char get_char(NBTBase nbt) {
		return nbt == null ? 0 : (char) ((NBTTagShort) nbt).data;
	}

	public static String get_java_lang_String(NBTBase nbt) {
		return nbt == null ? null : ((NBTTagString) nbt).data;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> Enum<?> get_java_lang_Enum(NBTBase nbt, Class<?> clazz) {
		return nbt == null ? null : Enum.valueOf((Class<E>) clazz, ((NBTTagString) nbt).data);
	}

	public static boolean[] get_boolean_arr(NBTBase nbt) {
		NBTTagCompound comp = (NBTTagCompound) nbt;
		return comp == null ? null : byteToBoolean(comp.getByteArray("b"), comp.getInteger("l"));
	}

	public static byte[] get_byte_arr(NBTBase nbt) {
		return nbt == null ? null : ((NBTTagByteArray) nbt).byteArray;
	}

	public static short[] get_short_arr(NBTBase nbt) {
		return nbt == null ? null : byteToShort(((NBTTagByteArray) nbt).byteArray);
	}

	public static int[] get_int_arr(NBTBase nbt) {
		return nbt == null ?null : ((NBTTagIntArray) nbt).intArray;
	}

	public static long[] get_long_arr(NBTBase nbt) {
		return nbt == null ? null : intToLong(((NBTTagIntArray) nbt).intArray);
	}

	public static float[] get_float_arr(NBTBase nbt) {
		return nbt == null ? null : intToFloat(((NBTTagIntArray) nbt).intArray);
	}

	public static double[] get_double_arr(NBTBase nbt) {
		return nbt == null ? null : intToDouble(((NBTTagIntArray) nbt).intArray);
	}

	public static char[] get_char_arr(NBTBase nbt) {
		return nbt == null ? null : byteToChar(((NBTTagByteArray) nbt).byteArray);
	}

	public static String[] get_java_lang_String_arr(NBTBase nbt) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		String[] result = new String[len];
		for (int i = 0; i < len; ++i) {
			result[i] = get_java_lang_String(list.tagAt(i));
		}
		return result;
	}

	public static Enum<?>[] get_java_lang_Enum_arr(NBTBase nbt, Class<?> clazz) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		Enum<?>[] result = (Enum<?>[]) Array.newInstance(clazz, len);
		for (int i = 0; i < len; ++i) {
			result[i] = get_java_lang_Enum(nbt, clazz);
		}
		return result;
	}

	public static Object[] get_deep_boolean(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}

		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			boolean[][] result = new boolean[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_boolean_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_boolean(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_byte(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			byte[][] result = new byte[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_byte_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_byte(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_short(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			short[][] result = new short[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_short_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_short(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_int(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			int[][] result = new int[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_int_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_int(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_long(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			long[][] result = new long[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_long_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_long(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_float(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			float[][] result = new float[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_float_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_float(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_double(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			double[][] result = new double[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_double_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);;
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_double(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_char(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			char[][] result = new char[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_char_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_char(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_java_lang_String(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			String[][] result = new String[len][];
			for (int i = 0; i < len; ++i) {
				result[i] = get_java_lang_String_arr(list.tagAt(i));
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_java_lang_String(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	public static Object[] get_deep_java_lang_Enum(NBTBase nbt, Class<?> fieldType, int dimensions) {
		if (nbt == null) {
			return null;
		}
		NBTTagList list = (NBTTagList) nbt;
		int len = list.tagCount();
		if (dimensions == 2) {
			Enum<?>[][] result = (Enum<?>[][]) Array.newInstance(fieldType, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_java_lang_Enum_arr(list.tagAt(i), fieldType.getComponentType());
			}
			return result;
		} else {
			Class<?> oneLessDim = fieldType.getComponentType();
			Object[] result = (Object[]) Array.newInstance(oneLessDim, len);
			for (int i = 0; i < len; ++i) {
				result[i] = get_deep_java_lang_Enum(list.tagAt(i), oneLessDim, dimensions - 1);
			}
			return result;
		}
	}

	private static byte[] booleanToByte(boolean[] value) {
		int numBytes = MathHelper.ceiling_float_int(value.length / 8f);
		int len = value.length;
		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < numBytes; ++i) {
			int off = i * 8;
			bytes[i] = (byte) ((off < len && value[off] ? 1 : 0)
					| (off + 1 < len && value[(off + 1)] ? 2 : 0)
					| (off + 2 < len && value[(off + 2)] ? 4 : 0)
					| (off + 3 < len && value[(off + 3)] ? 8 : 0)
					| (off + 4 < len && value[(off + 4)] ? 16  : 0)
					| (off + 5 < len && value[(off + 5)] ? 32 : 0)
					| (off + 6 < len && value[(off + 6)] ? 64 : 0)
					| (off + 7 < len && value[(off + 7)] ? 128 : 0));
		}

		return bytes;
	}

	private static boolean[] byteToBoolean(byte[] byteArr, int len) {
		boolean[] boolArr = new boolean[len];
		int byteLen = byteArr.length;
		for (int i = 0; i < byteLen; ++i) {
			byte b = byteArr[i];
			for (int off = i % 8, mask = 1; off < byteLen && off < 8; ++off) {
				boolArr[off] = (b & mask) != 0;
				mask <<= 1;
			}
		}
		return boolArr;
	}

	private static byte[] shortToByte(short[] value) {
		int sLen = value.length;
		byte[] bytes = new byte[sLen * 2];
		for (int sOff = 0, bOff = 0; sOff < sLen; ++sOff) {
			short s = value[sOff];
			bytes[bOff] = (byte) s;
			bytes[bOff + 1] = (byte) (s >> 8);
			bOff += 2;
		}
		return bytes;
	}

	private static short[] byteToShort(byte[] bytes) {
		int sLen = bytes.length / 2;
		short[] shorts = new short[sLen];
		for (int i = 0, bOff = 0; i < sLen; ++i) {
			shorts[i] = (short) (bytes[bOff] | (bytes[bOff + 1] << 8));
			bOff += 2;
		}
		return shorts;
	}

	// identical to shortToByte
	private static byte[] charToByte(char[] value) {
		int sLen = value.length;
		byte[] bytes = new byte[sLen * 2];
		for (int sOff = 0, bOff = 0; sOff < sLen; ++sOff) {
			char s = value[sOff];
			bytes[bOff] = (byte) s;
			bytes[bOff + 1] = (byte) (s >> 8);
			bOff += 2;
		}
		return bytes;
	}

	// identical to byteToShort
	private static char[] byteToChar(byte[] bytes) {
		int cLen = bytes.length / 2;
		char[] chars = new char[cLen];
		for (int i = 0, bOff = 0; i < cLen; ++i) {
			chars[i] = (char) (bytes[bOff] | (bytes[bOff + 1] << 8));
			bOff += 2;
		}
		return chars;
	}

	private static int[] longToInt(long[] value) {
		int lLen = value.length;
		int[] ints = new int[lLen * 2];
		for (int lOff = 0, iOff = 0; lOff < lLen; ++lOff) {
			long l = value[lOff];
			ints[iOff] = (int) l;
			ints[iOff + 1] = (int) (l >> 32);
			iOff += 2;
		}
		return ints;
	}

	private static long[] intToLong(int[] ints) {
		int lLen = ints.length << 1;
		long[] longs = new long[lLen];
		for (int i = 0, iOff = 0; i < lLen; ++i) {
			longs[i] = (((long) ints[iOff + 1]) << 32) | (ints[iOff] & 0xffffffffL);
			iOff += 2;
		}
		return longs;
	}

	private static int[] floatToInt(float[] value) {
		int len = value.length;
		int[] ints = new int[len];
		for (int i = 0; i < len; ++i) {
			ints[i] = Float.floatToIntBits(value[i]);
		}
		return ints;
	}

	private static float[] intToFloat(int[] ints) {
		int len = ints.length;
		float[] floats = new float[len];
		for (int i = 0; i < len; ++i) {
			floats[i] = Float.intBitsToFloat(ints[i]);
		}
		return floats;
	}

	private static int[] doubleToInt(double[] value) {
		int dLen = value.length;
		int[] ints = new int[dLen * 2];
		for (int lOff = 0, iOff = 0; lOff < dLen; ++lOff) {
			long l = Double.doubleToLongBits(value[lOff]);
			ints[iOff] = (int) l;
			ints[iOff + 1] = (int) (l >> 32);
			iOff += 2;
		}
		return ints;
	}

	private static double[] intToDouble(int[] ints) {
		int dLen = ints.length << 1;
		double[] doubles = new double[dLen];
		for (int i = 0, iOff = 0; i < dLen; ++i) {
			doubles[i] = Double.longBitsToDouble((((long) ints[iOff + 1]) << 32) | (ints[iOff] & 0xffffffffL));
			iOff += 2;
		}
		return doubles;
	}

}
