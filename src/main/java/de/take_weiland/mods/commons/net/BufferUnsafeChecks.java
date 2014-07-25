package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

import java.nio.ByteOrder;

/**
* @author diesieben07
*/
final class BufferUnsafeChecks {

	// factored out to avoid loading sun.misc.Unsafe class if it's not available
	static boolean checkUseable() {
		Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();
		// sanity checks to see if the native memory layout allows us to use the fast array copying
		if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN
			|| unsafe.arrayIndexScale(byte[].class) != 1
			|| unsafe.arrayIndexScale(short[].class) != 2
			|| unsafe.arrayIndexScale(int[].class) != 4
			|| unsafe.arrayIndexScale(long[].class) != 8
			|| unsafe.arrayIndexScale(float[].class) != 4
			|| unsafe.arrayIndexScale(double[].class) != 8) {
			return false;
		}

		long bits = Double.doubleToRawLongBits(3.4);
		double[] d = new double[1];
		unsafe.putLong(d, (long) unsafe.arrayBaseOffset(double[].class), bits);
		if (d[0] != 3.4) {
			return false;
		}

		int bits2 = Float.floatToRawIntBits(3.4f);
		float[] f = new float[1];
		unsafe.putInt(f, (long) unsafe.arrayBaseOffset(float[].class), bits2);
		return f[0] == 3.4f;
	}
}
