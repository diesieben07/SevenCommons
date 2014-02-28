package de.take_weiland.mods.commons.internal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.MathHelper;

import java.lang.reflect.Array;

/**
 * @author diesieben07
 */
public final class NBTASMHooks {

	public static void set(NBTTagCompound nbt, String key, boolean value) {
		nbt.setBoolean(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, byte value) {
		nbt.setByte(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, short value) {
		nbt.setShort(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, int value) {
		nbt.setInteger(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, long value) {
		nbt.setLong(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, float value) {
		nbt.setFloat(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, double value) {
		nbt.setDouble(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, char value) {
		nbt.setShort(key, (short) value);
	}

	public static void set(NBTTagCompound nbt, String key, String value) {
		nbt.setString(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, Enum<?> value) {
		nbt.setString(key, value.name());
	}

	public static void set(NBTTagCompound nbt, String key, boolean[] value) {
		byte[] bytes = booleanToByte(value);
		nbt.setInteger(key + "_sc$boolArrLen", value.length);
		nbt.setByteArray(key, bytes);
	}

	private static byte[] booleanToByte(boolean[] value) {
		int numBytes = MathHelper.ceiling_float_int(value.length / 8f);
		int len = value.length;
		byte[] bytes = new byte[numBytes];
		for (int i = 0; i < numBytes; ++i) {
			int off = i * 8;
			bytes[i] = (byte) ((safe(value, len, off) ? 1 : 0)
					| (safe(value, len, off + 1) ? 2 : 0)
					| (safe(value, len, off + 2) ? 4 : 0)
					| (safe(value, len, off + 3) ? 8 : 0)
					| (safe(value, len, off + 4) ? 16  : 0)
					| (safe(value, len, off + 5) ? 32 : 0)
					| (safe(value, len, off + 6) ? 64 : 0)
					| (safe(value, len, off + 7) ? 128 : 0));
		}

		return bytes;
	}

	private static boolean safe(boolean[] arr, int len, int idx) {
		return idx < len && arr[idx];
	}

	public static void set(NBTTagCompound nbt, String key, byte[] value) {
		nbt.setByteArray(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, short[] value) {
		int len = value.length;
		byte[] bytes = new byte[len * 2];
		for (int i = 0; i < len; ++i) {
			int off = i * 2;
			short v = value[i];
			bytes[off] = (byte) (v & 0xFF);
			bytes[off + 1] = (byte) ((v >> 2) & 0xFF);
		}
		nbt.setByteArray(key, bytes);
	}

	public static void set(NBTTagCompound nbt, String key, int[] value) {
		nbt.setIntArray(key, value);
	}

	public static void set(NBTTagCompound nbt, String key, long[] value) {
		int len = value.length;
		int[] ints = new int[len * 2];
		for (int i = 0; i < len; ++i) {
			int off = i * 2;
			long v = value[i];
			ints[off] = (int) (v >> 32);
			ints[off + 1] = (int) v;
		}
		nbt.setIntArray(key, ints);
	}

	public static void set(NBTTagCompound nbt, String key, float[] value) {
		int len = value.length;
		int[] ints = new int[len];
		for (int i = 0; i < len; ++i) {
			ints[i] = Float.floatToIntBits(value[i]);
		}
		nbt.setIntArray(key, ints);
	}

	public static void set(NBTTagCompound nbt, String key, double[] value) {
		int len = value.length;
		int[] ints = new int[len * 2];
		for (int i = 0; i < len; ++i) {
			int off = i * 2;
			long v = Double.doubleToLongBits(value[i]);
			ints[off] = (int) (v >> 32);
			ints[off + 1] = (int) v;
		}
		nbt.setIntArray(key, ints);
	}

	public static void set(NBTTagCompound nbt, String key, char[] value) {
		int len = value.length;
		byte[] bytes = new byte[len * 2];
		for (int i = 0; i < len; ++i) {
			int off = i * 2;
			char v = value[i];
			bytes[off] = (byte) (v & 0xFF);
			bytes[off + 1] = (byte) ((v >> 2) & 0xFF);
		}
		nbt.setByteArray(key, bytes);
	}

	public static void set(NBTTagCompound nbt, String key, String[] value) {
		NBTTagList list = new NBTTagList();
		for (String v : value) {
			list.appendTag(new NBTTagString(null, v));
		}
		nbt.setTag(key, list);
	}

	public static void set(NBTTagCompound nbt, String key, Enum<?>[] value) {
		NBTTagList list = new NBTTagList();
		for (Enum<?> aValue : value) {
			list.appendTag(new NBTTagString(null, aValue.name()));
		}
		nbt.setTag(key, list);
	}

	public static boolean get_boolean(NBTTagCompound nbt, String key) {
		return nbt.getBoolean(key);
	}

	public static byte get_byte(NBTTagCompound nbt, String key) {
		return nbt.getByte(key);
	}

	public static short get_short(NBTTagCompound nbt, String key) {
		return nbt.getShort(key);
	}

	public static int get_int(NBTTagCompound nbt, String key) {
		return nbt.getInteger(key);
	}

	public static long get_long(NBTTagCompound nbt, String key) {
		return nbt.getLong(key);
	}

	public static float get_float(NBTTagCompound nbt, String key) {
		return nbt.getFloat(key);
	}

	public static double get_double(NBTTagCompound nbt, String key) {
		return nbt.getDouble(key);
	}

	public static char get_char(NBTTagCompound nbt, String key) {
		return (char) nbt.getShort(key);
	}

	public static String get_java_lang_String(NBTTagCompound nbt, String key) {
		return nbt.getString(key);
	}

	public static <E extends Enum<E>> Enum<?> get_java_lang_Enum(NBTTagCompound nbt, String key, Class<E> enumClass) {
		String name = nbt.getString(key);
		return Enum.valueOf(enumClass, name);
	}

	public static boolean[] get_boolean_arr(NBTTagCompound nbt, String key) {
		return byteToBoolean(nbt.getByteArray(key), nbt.getInteger(key + "_sc$boolArrLen"));
	}

	private static boolean[] byteToBoolean(byte[] bytes, int len) {
		boolean[] b = new boolean[len];
		int bOffset = 0;
		byte value = 0;
		for (int i = 0; i < len; ++i) {
			int rem = i % 8;
			if (rem == 0) {
				value = bytes[bOffset++];
			}
			int flag = 1 << rem;
			b[i] = (value & flag) != 0;
		}

		return b;
	}

	public static byte[] get_byte_arr(NBTTagCompound nbt, String key) {
		return nbt.getByteArray(key);
	}

	public static short[] get_short_arr(NBTTagCompound nbt, String key) {
		byte[] bytes = nbt.getByteArray(key);
		int len = bytes.length;
		short[] shorts = new short[len / 2];
		for (int i = 0; i < len; i += 2) {
			shorts[i / 2] = (short) (bytes[i] | (bytes[i + 1] << 2));
		}
		return shorts;
	}

	public static int[] get_int_arr(NBTTagCompound nbt, String key) {
		return nbt.getIntArray(key);
	}

	public static long[] get_long_arr(NBTTagCompound nbt, String key) {
		int[] ints = nbt.getIntArray(key);
		int len = ints.length;
		long[] longs = new long[len / 2];
		for (int i = 0; i < len; i += 2) {
			longs[i / 2] = (((long) ints[i]) << 32) | (ints[i + 1] & 0xffffffffL);
		}
		return longs;
	}

	public static float[] get_float_arr(NBTTagCompound nbt, String key) {
		int[] ints = nbt.getIntArray(key);
		int len = ints.length;
		float[] floats = new float[len];
		for (int i = 0; i < len; ++i) {
			floats[i] = Float.intBitsToFloat(ints[i]);
		}
		return floats;
	}

	public static double[] get_double_arr(NBTTagCompound nbt, String key) {
		int[] ints = nbt.getIntArray(key);
		int len = ints.length;
		double[] d = new double[len / 2];
		for (int i = 0; i < len; i += 2) {
			d[i / 2] = Double.longBitsToDouble((((long) ints[i]) << 32) | (ints[i + 1] & 0xffffffffL));
		}
		return d;
	}

	public static char[] get_char_arr(NBTTagCompound nbt, String key) {
		byte[] bytes = nbt.getByteArray(key);
		int len = bytes.length;
		char[] chars = new char[len / 2];
		for (int i = 0; i < len; i += 2) {
			byte b1 = bytes[i];
			byte b2 = bytes[i + 1];
			chars[i / 2] = (char) (b1 | (b2 << 2));
		}
		return chars;
	}

	public static String[] get_java_lang_String_arr(NBTTagCompound nbt, String key) {
		NBTTagList list = nbt.getTagList(key);
		int len = list.tagCount();
		String[] s = new String[len];
		for (int i = 0; i < len; ++i) {
			s[i] = ((NBTTagString)list.tagAt(i)).data;
		}
		return s;
	}

	public static <T extends Enum<T>> Enum<?>[] get_java_lang_Enum_arr(NBTTagCompound nbt, String key, Class<T> enumClazz) {
		NBTTagList list = nbt.getTagList(key);
		int len = list.tagCount();
		@SuppressWarnings("unchecked")
		T[] arr = (T[]) Array.newInstance(enumClazz, len);
		for (int i = 0; i < len; ++i) {
			arr[i] = Enum.valueOf(enumClazz, ((NBTTagString)list.tagAt(i)).data);
		}
		return arr;
	}

}
