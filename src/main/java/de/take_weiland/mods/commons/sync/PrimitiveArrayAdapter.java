package de.take_weiland.mods.commons.sync;

import java.util.Arrays;

/**
 * @author diesieben07
 */
abstract class PrimitiveArrayAdapter<T> extends SyncAdapter<T> {

	static class OfBoolean extends PrimitiveArrayAdapter<boolean[]> {

		private boolean[] array;

		@Override
		public boolean checkAndUpdate(boolean[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new boolean[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfByte extends PrimitiveArrayAdapter<byte[]> {

		private byte[] array;

		@Override
		public boolean checkAndUpdate(byte[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new byte[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfShort extends PrimitiveArrayAdapter<short[]> {

		private short[] array;

		@Override
		public boolean checkAndUpdate(short[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new short[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfInt extends PrimitiveArrayAdapter<int[]> {

		private int[] array;

		@Override
		public boolean checkAndUpdate(int[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new int[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfLong extends PrimitiveArrayAdapter<long[]> {

		private long[] array;

		@Override
		public boolean checkAndUpdate(long[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new long[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfChar extends PrimitiveArrayAdapter<char[]> {

		private char[] array;

		@Override
		public boolean checkAndUpdate(char[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new char[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfFloat extends PrimitiveArrayAdapter<float[]> {

		private float[] array;

		@Override
		public boolean checkAndUpdate(float[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new float[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

	static class OfDouble extends PrimitiveArrayAdapter<double[]> {

		private double[] array;

		@Override
		public boolean checkAndUpdate(double[] newValue) {
			if (Arrays.equals(array, newValue)) {
				return false;
			} else {
				int len = newValue.length;
				if (array.length != len) {
					array = new double[len];
				}
				System.arraycopy(newValue, 0, array, 0, len);
				return false;
			}
		}
	}

}
