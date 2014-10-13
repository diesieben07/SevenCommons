package de.take_weiland.mods.commons.nbt;

import org.junit.Test;

import static de.take_weiland.mods.commons.nbt.ArrayConversions.decodeBooleans;
import static de.take_weiland.mods.commons.nbt.ArrayConversions.encodeBooleans;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestArrayConversions {

	@Test
	public void testEncodeBooleans() throws Exception {
		boolean[] data = { true, false, false, true, false, true, true, false, true};
		byte[] expected = { 0b0110_1001, 0b0000_0001 };
		assertThat(encodeBooleans(data), is(expected));
	}

	@Test
	public void testDecodeBooleans() {
		byte[] data = { 0b0110_1001, 0b0000_0001 };
		boolean[] expected = { true, false, false, true, false, true, true, false, true};
		assertThat(decodeBooleans(data, 9),	is(expected));
	}
}