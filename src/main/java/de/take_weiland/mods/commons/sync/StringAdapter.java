package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;

/**
 * @author diesieben07
 */
class StringAdapter extends ImmutableAdapter<String> {

	@Override
	public void write(String value, PacketBuilder builder) {
		builder.writeString(value);
	}

	@SuppressWarnings("unchecked") // can't extend String
	@Override
	public <ACTUAL_T extends String> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
		return ((ACTUAL_T) buf.readString());
	}
}
