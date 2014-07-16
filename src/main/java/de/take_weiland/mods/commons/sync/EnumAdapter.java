package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.PacketBuilder;

/**
 * @author diesieben07
 */
class EnumAdapter extends SyncAdapter<Enum<?>> {

	private Enum<?> value;

	@Override
	public boolean checkAndUpdate(Enum<?> newValue) {
		if (value != newValue) {
			value = newValue;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void write(Enum<?> value, PacketBuilder builder) {
		DataBuffers.writeEnum(builder, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <ACTUAL_T extends Enum<?>> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
		return (ACTUAL_T) DataBuffers.readEnum(buf, prevVal.getDeclaringClass());
	}
}
