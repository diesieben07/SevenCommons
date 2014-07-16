package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;

import java.util.UUID;

/**
 * @author diesieben07
 */
class UUIDAdapter extends ImmutableAdapter<UUID> {
	@Override
	public void write(UUID value, PacketBuilder builder) {
		builder.writeLong(value.getMostSignificantBits());
		builder.writeLong(value.getLeastSignificantBits());
	}

	@SuppressWarnings("unchecked") // UUID is final
	@Override
	public <ACTUAL_T extends UUID> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
		return (ACTUAL_T) new UUID(buf.readLong(), buf.readLong());
	}
}
