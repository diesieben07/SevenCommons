package de.take_weiland.mods.commons.net;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author diesieben07
 */
public abstract class MCDataOuputTest {

	abstract FastBAOS newStream();

	public static class ForUnsafe extends MCDataOuputTest {

		@Override
		FastBAOS newStream() {
			return new FastBAOSUnsafe();
		}

	}

	public static class NonUnsafe extends MCDataOuputTest {

		@Override
		FastBAOS newStream() {
			return new FastBAOSNonUnsafe();
		}

	}

	@Test
	public void testByte() {
		FastBAOS stream = newStream();
		stream.writeByte(0b0100_1101);
		assertArrayEquals(new byte[] { (byte) 0b0100_1101 }, stream.toByteArray());
	}

	@Test
	public void testShort() {
		FastBAOS stream = newStream();
		stream.writeShort(0x77D0);
		assertArrayEquals(new byte[] { (byte) 0xD0, (byte) 0x77 }, stream.toByteArray());
	}

	@Test
	public void testInt() {
		FastBAOS stream = newStream();
		stream.writeInt(0x77D0_FA34);
		assertArrayEquals(new byte[] { (byte) 0x34, (byte) 0xFA, (byte) 0xD0, (byte) 0x77 }, stream.toByteArray());
	}

}
