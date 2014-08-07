package de.take_weiland.mods.commons.net;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author diesieben07
 */
public abstract class MCDataInputTest {

	abstract MCDataInputImpl createStream(byte... buf);

	public static class ForUnsafe extends MCDataInputTest {

		@Override
		MCDataInputImpl createStream(byte[] buf) {
			return new MCDataInputImplUnsafe(buf, 0, buf.length);
		}
	}

	public static class NonUnsafe extends MCDataInputTest {

		@Override
		MCDataInputImpl createStream(byte[] buf) {
			return new MCDataInputImplNonUnsafe(buf, 0, buf.length);
		}
	}

	@Test(expected = NullPointerException.class)
	public void testNullArray() {
		MCDataInputStream.create(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullArray2() {
		MCDataInputStream.create(null, 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeLength() {
		MCDataInputStream.create(new byte[0], 0, -3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeOffset() {
		MCDataInputStream.create(new byte[2], -1, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testOutOfRangeOffset() {
		MCDataInputStream.create(new byte[2], 3, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testOutOfRangeLength() {
		MCDataInputStream.create(new byte[2], 0, 4);
	}

	static byte[] leArr(long l) {
		byte[] arr = Longs.toByteArray(l);
		ArrayUtils.reverse(arr);
		return arr;
	}

	static byte[] leArr(int l) {
		byte[] arr = Ints.toByteArray(l);
		ArrayUtils.reverse(arr);
		return arr;
	}

	private static byte[] leArr(short l) {
		byte[] arr = Shorts.toByteArray(l);
		ArrayUtils.reverse(arr);
		return arr;
	}

	private static byte[] leArr(char l) {
		byte[] arr = Chars.toByteArray(l);
		ArrayUtils.reverse(arr);
		return arr;
	}

	@Test
	public void testByte() {
		MCDataInputImpl stream = createStream(
				(byte) 0b0011_0111,
				(byte) 0b1010_0101,
				(byte) 0b1111_0001);

		assertEquals((byte) 0b0011_0111, stream.readByte());
		assertEquals((byte) 0b1010_0101, stream.readByte());
		assertEquals((byte) 0b1111_0001, stream.readByte());
	}

	@Test
	 public void testShort() {
		checkShort((short) 0x7F);
		checkShort((short) 0xF7);
		checkShort((short) 0xFF);
		checkShort((short) 0x00);
		checkShort((short) 0x08);
		checkShort((short) 0x80);
	}

	private void checkShort(short expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(expected)).readShort());
	}

	@Test
	public void testInt() {
		checkInt(0x0000);
		checkInt(0x000F);
		checkInt(0x00F0);
		checkInt(0x0F00);
		checkInt(0xF000);
		checkInt(0xFF00);
		checkInt(0x00FF);
		checkInt(0x7F00);
		checkInt(0xF700);
		checkInt(0x007F);
		checkInt(0x00F7);
		checkInt(0x0008);
		checkInt(0x0080);
		checkInt(0x0800);
		checkInt(0x8000);
		checkInt(0xF780);
		checkInt(0x80F7);
	}

	private void checkInt(int expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(expected)).readInt());
	}

	@Test
	public void testLong() {
		checkLong(0x0000_0000L);
		checkLong(0xFFFF_FFFFL);
		checkLong(0xF000_0000L);
		checkLong(0x0000_000FL);
		checkLong(0xFFFF_0000L);
		checkLong(0x0000_FFFFL);
		checkLong(0x7F00_0000L);
		checkLong(0x0000_007FL);
		checkLong(0x0800_0000L);
		checkLong(0x0000_8000L);
	}


	private void checkLong(long expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(expected)).readLong());
	}

	@Test
	public void testChar() {
		checkChar((char) 0x7F);
		checkChar((char) 0xF7);
		checkChar((char) 0xFF);
		checkChar((char) 0x00);
		checkChar((char) 0x08);
		checkChar((char) 0x80);
	}

	private void checkChar(char expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(expected)).readChar());
	}

	@Test
	public void testFloat() {
		checkFloat(1.24f);
	}

	private void checkFloat(float expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(Float.floatToRawIntBits(expected))).readFloat(), Double.POSITIVE_INFINITY);
	}

	@Test
	public void testDouble() {
		checkDouble(1.24);
	}

	private void checkDouble(double expected) {
		assertEquals(expected + " == " + expected, expected, createStream(leArr(Double.doubleToRawLongBits(expected))).readDouble(), Double.POSITIVE_INFINITY);
	}

	@Test
	public void testBooleans() {
		assertTrue(Arrays.equals(new boolean[]{
						true, false, true, true
				},
				createStream(
						(byte) 0b1000_0100,
						(byte) 0b0000_1101
				).readBooleans()));

		assertTrue(Arrays.equals(new boolean[]{
				true, false, true, true, false, false, true, true,
				false, true, false, true, false, false
		}, createStream(
				(byte) 0b1000_1110,
				(byte) 0b11001101,
				(byte) 0b00001010
		).readBooleans()));

		assertNull(createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		).readBooleans());
	}

	@Test
	public void testBytes() {
		assertArrayEquals(
				new byte[] { (byte) 0b0101_1010, (byte) 0b1010_0101 },
				createStream(
						(byte) 0b1000_0010, // varInt: 2
						(byte) 0b0101_1010,
						(byte) 0b1010_0101
				).readBytes()
		);

		assertNull(createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		).readBytes());
	}

	@Test
	public void testShorts() {
		assertArrayEquals(
				new short[] { (short) 0xEA40, (short) 0x21F0 },
				createStream(
						(byte) 0b1000_0010, // varInt: 2
						(byte) 0x40, // shorts in little endian
						(byte) 0xEA,
						(byte) 0xF0,
						(byte) 0x21).readShorts()
		);

		assertNull(createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		).readShorts());
	}

	@Test
	public void testInts() {
		assertArrayEquals(
				new int[] { 0xEA4021F0, 0x0F3407F8 },
				createStream(
						(byte) 0b1000_0010, // varInt: 2
						(byte) 0xF0,
						(byte) 0x21,
						(byte) 0x40,
						(byte) 0xEA,

						(byte) 0xF8,
						(byte) 0x07,
						(byte) 0x34,
						(byte) 0x0F).readInts()
		);

		assertNull(createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		).readInts());
	}

	@Test
	public void testLongs() {
		assertArrayEquals(
				new long[] { 0x12_34_56_78_9A_BC_DE_F0L, 0x0F_ED_CB_A9_87_65_43_21L },
				createStream(
						(byte) 0b1000_0010, // varInt: 2

						(byte) 0xF0,
						(byte) 0xDE,
						(byte) 0xBC,
						(byte) 0x9A,
						(byte) 0x78,
						(byte) 0x56,
						(byte) 0x34,
						(byte) 0x12,

						(byte) 0x21,
						(byte) 0x43,
						(byte) 0x65,
						(byte) 0x87,
						(byte) 0xA9,
						(byte) 0xCB,
						(byte) 0xED,
						(byte) 0x0F).readLongs()
		);

		assertNull(createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		).readLongs());
	}

}
