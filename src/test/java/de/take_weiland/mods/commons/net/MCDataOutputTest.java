package de.take_weiland.mods.commons.net;

import org.junit.Test;

import static de.take_weiland.mods.commons.net.MCDataInputTest.leArr;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author diesieben07
 */
public abstract class MCDataOutputTest {

	abstract MCDataOutputImpl newStream();
	abstract MCDataOutputImpl newStream(int cap);

	public static class NonUnsafe extends MCDataOutputTest {

		@Override
		MCDataOutputImpl newStream() {
			return new MCDataOutputImplNonUnsafe(64);
		}

		@Override
		MCDataOutputImpl newStream(int cap) {
			return new MCDataOutputImplNonUnsafe(cap);
		}
	}

	@Test
	public void testInitialCapacity() {
		assertEquals("user provided capacity", 126, newStream(126).buf.length);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalInitialCapacity() {
		MCDataOutputStream.create(-1);
	}

	@Test(expected = RuntimeException.class)
	public void testLock() {
		MCDataOutputStream stream = newStream();
		stream.lock();
		stream.write(0);
	}

	@Test
	public void testByte() {
		MCDataOutputImpl stream = newStream();
		stream.writeByte(0b0100_1101);
		assertArrayEquals(new byte[] { (byte) 0b0100_1101 }, stream.toByteArray());
	}

	@Test
	public void testShort() {
		MCDataOutputImpl stream = newStream();
		stream.writeShort(0x77D0);
		assertArrayEquals(new byte[] { (byte) 0xD0, (byte) 0x77 }, stream.toByteArray());
	}

	@Test
	public void testInt() {
		MCDataOutputImpl stream = newStream();
		stream.writeInt(0x77D0_FA34);
		assertArrayEquals(new byte[] { (byte) 0x34, (byte) 0xFA, (byte) 0xD0, (byte) 0x77 }, stream.toByteArray());
	}

	@Test
	public void testLong() {
		MCDataOutputImpl stream = newStream();
		stream.writeLong(0x77D0_FA34_4508_E044L);
		assertArrayEquals(new byte[] {
				(byte) 0x44,
				(byte) 0xE0,
				(byte) 0x08,
				(byte) 0x45,
				(byte) 0x34,
				(byte) 0xFA,
				(byte) 0xD0,
				(byte) 0x77
		}, stream.toByteArray());
	}

	@Test
	public void testChar() {
		MCDataOutputImpl stream = newStream();
		stream.writeChar(0x77D0);
		assertArrayEquals(new byte[] { (byte) 0xD0, (byte) 0x77 }, stream.toByteArray());
	}

	@Test
	public void testFloat() {
		MCDataOutputImpl stream = newStream();
		stream.writeFloat(3.5f);
		assertArrayEquals(leArr(Float.floatToRawIntBits(3.5f)), stream.toByteArray());
	}

	@Test
	public void testDouble() {
		MCDataOutputImpl stream = newStream();
		stream.writeDouble(3.5);
		assertArrayEquals(leArr(Double.doubleToRawLongBits(3.5)), stream.toByteArray());
	}

	@Test
	public void testBytes() {
		MCDataOutputImpl stream = newStream();
		stream.writeBytes(new byte[] { 77, (byte) 0xFE, (byte) 0xAA }, 1, 2);
		assertArrayEquals(new byte[] {
				(byte) 0b1000_0010,
				(byte) 0xFE,
				(byte) 0xAA
		}, stream.toByteArray());
	}

	@Test
	public void testShorts() {
		MCDataOutputImpl stream = newStream();
		stream.writeShorts(new short[] { 1234, 0x45_FE, 0x21_90 }, 1, 2);
		assertArrayEquals(new byte[] {
				(byte) 0b1000_0010,
				(byte) 0xFE,
				(byte) 0x45,
				(byte) 0x90,
				(byte) 0x21
		}, stream.toByteArray());
	}

	@Test
	public void testInts() {
		MCDataOutputImpl stream = newStream();
		stream.writeInts(new int[] { 12345, 0x45FE_2190, 0x6070_34AB }, 1, 2);
		assertArrayEquals(new byte[] {
				(byte) 0b1000_0010,
				(byte) 0x90,
				(byte) 0x21,
				(byte) 0xFE,
				(byte) 0x45,
				(byte) 0xAB,
				(byte) 0x34,
				(byte) 0x70,
				(byte) 0x60
		}, stream.toByteArray());
	}

	@Test
	public void testLongs() {
		MCDataOutputImpl stream = newStream();
		stream.writeLongs(new long[] { 12345L, 0x45FE_2190_6070_34ABL, 0xA412_B5E0_77A3_220FL }, 1, 2);
		assertArrayEquals(new byte[] {
				(byte) 0b1000_0010,
				(byte) 0xAB,
				(byte) 0x34,
				(byte) 0x70,
				(byte) 0x60,
				(byte) 0x90,
				(byte) 0x21,
				(byte) 0xFE,
				(byte) 0x45,

				(byte) 0x0F,
				(byte) 0x22,
				(byte) 0xA3,
				(byte) 0x77,
				(byte) 0xE0,
				(byte) 0xB5,
				(byte) 0x12,
				(byte) 0xA4
		}, stream.toByteArray());
	}

	@Test
	public void testBooleans() {
		MCDataOutputImpl stream = newStream();
		stream.writeBooleans(new boolean[] { true, false, true, true, false, true, false, false, false, true }, 1, 9);
		assertArrayEquals(new byte[] {
				(byte) 0b1000_1001,
				(byte) 0b00010110,
				(byte) 0b00000001
		}, stream.toByteArray());
	}

	@Test
	public void testVarInt() {
		assertArrayEquals(new byte[] {
				(byte) 0b1000_0000
		}, varIntBytes(0));

		assertArrayEquals(new byte[] {
				(byte) 0b1000_0001
		}, varIntBytes(1));

		assertArrayEquals(new byte[] {
				(byte) 0b0111_1111,
				(byte) 0b1000_0001
		}, varIntBytes(0xFF));

		assertArrayEquals(new byte[] {
				(byte) 0b1111_1111
		}, varIntBytes(0b0111_1111));

		assertArrayEquals(new byte[] {
				(byte) 0b0000_0000,
				(byte) 0b0000_0000,
				(byte) 0b1000_0001
		}, varIntBytes(0b1_0000000_0000000));
	}

	private byte[] varIntBytes(int i) {
		MCDataOutputImpl stream = newStream();
		stream.writeVarInt(i);
		return stream.toByteArray();
	}

}
