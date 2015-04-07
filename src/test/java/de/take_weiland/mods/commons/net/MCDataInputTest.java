package de.take_weiland.mods.commons.net;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

/**
 * @author diesieben07
 */
public class MCDataInputTest {

	MCDataInputImpl createStream(byte... buf) {
		return new MCDataInputImpl(buf, 0, buf.length);
	}

	@SuppressWarnings("ConstantConditions")
	@Test(expected = NullPointerException.class)
	public void testNullArray() {
		Network.newDataInput(null);
	}

	@SuppressWarnings("ConstantConditions")
	@Test(expected = NullPointerException.class)
	public void testNullArray2() {
		Network.newDataInput(null, 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeLength() {
		Network.newDataInput(new byte[0], 0, -3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeOffset() {
		Network.newDataInput(new byte[2], -1, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testOutOfRangeOffset() {
		Network.newDataInput(new byte[2], 3, 2);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testOutOfRangeLength() {
		Network.newDataInput(new byte[2], 0, 4);
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

	private static byte[] leArr(float f) {
		byte[] arr = Ints.toByteArray(Float.floatToIntBits(f));
		ArrayUtils.reverse(arr);
		return arr;
	}

	private static byte[] leArr(double d) {
		byte[] arr = Longs.toByteArray(Double.doubleToLongBits(d));
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
		assertThat(createStream(leArr(expected)).readShort(), is(equalTo(expected)));
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
		assertThat(createStream(leArr(expected)).readInt(), is(equalTo(expected)));
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
		assertThat(createStream(leArr(expected)).readLong(), is(equalTo(expected)));
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
		assertThat(createStream(leArr(expected)).readChar(), is(equalTo(expected)));
	}

	@Test
	public void testFloat() {
		checkFloat(1.24f);
	}

	private void checkFloat(float expected) {
		assertThat(createStream(leArr(expected)).readFloat(), is(equalTo(expected)));
	}

	@Test
	public void testDouble() {
		checkDouble(1.24);
	}

	private void checkDouble(double expected) {
		assertThat(createStream(leArr(expected)).readDouble(), is(equalTo(expected)));
	}

	@Test
	public void testBooleans1Byte() {
		MCDataInputImpl stream = createStream(
				(byte) 0b1000_0100,
				(byte) 0b0000_1101
		);
		boolean[] expected = { true, false, true, true };

		assertThat(stream.readBooleans(), is(equalTo(expected)));
	}

	@Test
	public void testBooleans2Byte() {
		MCDataInputImpl stream = createStream(
				(byte) 0b1000_1110,
				(byte) 0b11001101,
				(byte) 0b00001010
		);
		boolean[] expected = {
				true, false, true, true, false, false, true, true,
				false, true, false, true, false, false
		};

		assertThat(stream.readBooleans(), is(equalTo(expected)));
	}

	@Test
	public void testBooleansNull() {
		MCDataInputImpl stream = minusOneStream();

		assertThat(stream.readBooleans(), is(nullValue()));
	}

	@Test
	public void testBytes() {
		byte[] expected = {(byte) 0b0101_1010, (byte) 0b1010_0101};
		MCDataInputImpl stream = createStream(
				(byte) 0b1000_0010, // varInt: 2
				(byte) 0b0101_1010,
				(byte) 0b1010_0101
		);

		assertThat(stream.readBytes(), is(equalTo(expected)));
	}

	@Test
	public void testNullBytes() {
		assertThat(minusOneStream().readBytes(), is(nullValue()));
	}

	@Test
	public void testShorts() {
		short[] expected = {(short) 0xEA40, (short) 0x21F0};
		MCDataInputImpl stream = createStream(
				(byte) 0b1000_0010, // varInt: 2
				(byte) 0x40, // shorts in little endian
				(byte) 0xEA,
				(byte) 0xF0,
				(byte) 0x21);

		assertThat(stream.readShorts(), is(equalTo(expected)));
	}

	@Test
	public void testNullShorts() {
		assertThat(minusOneStream().readShorts(), is(nullValue()));
	}

	@Test
	public void testInts() {
		int[] expected = {0xEA4021F0, 0x0F3407F8};
		MCDataInputImpl stream = createStream(
				(byte) 0b1000_0010, // varInt: 2
				(byte) 0xF0,
				(byte) 0x21,
				(byte) 0x40,
				(byte) 0xEA,

				(byte) 0xF8,
				(byte) 0x07,
				(byte) 0x34,
				(byte) 0x0F);

		assertThat(stream.readInts(), is(equalTo(expected)));
	}

	@Test
	public void testNullInts() {
		assertThat(minusOneStream().readInts(), is(nullValue()));
	}

	@Test
	public void testLongs() {
		long[] expected = {0x12_34_56_78_9A_BC_DE_F0L, 0x0F_ED_CB_A9_87_65_43_21L};
		MCDataInputImpl stream = createStream(
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
				(byte) 0x0F);

		assertThat(stream.readLongs(), is(equalTo(expected)));
	}

	@Test
	public void testNullLongs() {
		assertThat(minusOneStream().readLongs(), is(nullValue()));
	}

	final MCDataInputImpl minusOneStream() {
		return createStream(
				(byte) 0b0111_1111, // VarInt: -1
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b0111_1111,
				(byte) 0b1000_1111
		);
	}

}
