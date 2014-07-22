package de.take_weiland.mods.commons.net;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author diesieben07
 */
public abstract class MCDataInputArrayTest {

	abstract FastBAIS createStream(byte[] buf);

	public static class ForUnsafe extends MCDataInputArrayTest {

		@Override
		FastBAIS createStream(byte[] buf) {
			return new FastBAISUnsafe(buf, 0, buf.length);
		}
	}

	public static class NonUnsafe extends MCDataInputArrayTest {

		@Override
		FastBAIS createStream(byte[] buf) {
			return new FastBAISNonUnsafe(buf, 0, buf.length);
		}
	}

	private static final byte byte1 = 0b0101_1010;
	private static final byte byte2 = (byte) 0b1010_0101;
	private static final byte[] bytes = { byte1, byte2 };
	private static final byte[] byteBuf = {
			(byte) 0b1000_0010, // varInt: 2
			byte1, byte2
	};

	@Test
	public void testBytes() {
		assertArrayEquals(bytes, createStream(byteBuf).readBytes());
	}

	private static final short short1 = 0b0001_0010_0100_1000;
	private static final short short2 = (short) 0b1000_0100_0010_0001;
	private static final short[] shorts = { short1 , short2 };
	private static final byte[] shortBuf = {
			(byte) 0b1000_0010, // varInt: 2
			(byte) (short1 & 0xFF), // shorts in little endian
			(byte) ((short1 >> 8) & 0xFF),
			(byte) (short2 & 0xFF),
			(byte) ((short2 >> 8) & 0xFF),
	};

	@Test
	public void testShorts() {
		assertArrayEquals(shorts, createStream(shortBuf).readShorts());
	}

	private static final int int1 = 0b0001_0010_0100_1000_1000_0010_0100_1000;
	private static final int int2 = 0b1000_0100_0010_0001_0001_0100_0100_0001;
	private static final int[] ints = { int1, int2 };
	private static final byte[] intBuf = {
			(byte) 0b1000_0010, // varInt: 2
			(byte) (int1 & 0xFF),
			(byte) ((int1 >> 8) & 0xFF),
			(byte) ((int1 >> 16) & 0xFF),
			(byte) ((int1 >> 24) & 0xFF),
			(byte) (int2 & 0xFF),
			(byte) ((int2 >> 8) & 0xFF),
			(byte) ((int2 >> 16) & 0xFF),
			(byte) ((int2 >> 24) & 0xFF),
	};

	@Test
	public void testInts() {
		assertArrayEquals(ints, createStream(intBuf).readInts());
	}

	private static final long long1 = 0x12_34_56_78_9A_BC_DE_F0L;
	private static final long long2 = 0x0F_ED_CB_A9_87_65_43_21L;
	private static final long[] longs = { long1, long2 };
	private static final byte[] longBuf = {
			(byte) 0b1000_0010, // varInt: 2

			(byte) (long1 & 0xFF),
			(byte) ((long1 >> 8) & 0xFF),
			(byte) ((long1 >> 16) & 0xFF),
			(byte) ((long1 >> 24) & 0xFF),
			(byte) ((long1 >> 32) & 0xFF),
			(byte) ((long1 >> 40) & 0xFF),
			(byte) ((long1 >> 48) & 0xFF),
			(byte) ((long1 >> 56) & 0xFF),

			(byte) (long2 & 0xFF),
			(byte) ((long2 >> 8) & 0xFF),
			(byte) ((long2 >> 16) & 0xFF),
			(byte) ((long2 >> 24) & 0xFF),
			(byte) ((long2 >> 32) & 0xFF),
			(byte) ((long2 >> 40) & 0xFF),
			(byte) ((long2 >> 48) & 0xFF),
			(byte) ((long2 >> 56) & 0xFF),

	};

	@Test
	public void testLongs() {
		assertArrayEquals(longs, createStream(longBuf).readLongs());
	}

}
