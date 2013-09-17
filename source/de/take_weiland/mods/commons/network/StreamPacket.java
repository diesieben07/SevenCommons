package de.take_weiland.mods.commons.network;

import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataInputImpl;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;
import de.take_weiland.mods.commons.util.MinecraftDataOutputImpl;

public abstract class StreamPacket extends AbstractModPacket {

	protected abstract void writeData(MinecraftDataOutput out);
	
	protected abstract void readData(MinecraftDataInput in);
	
	protected int expectedSize() {
		return 32;
	}
	
	@Override
	public final byte[] getData(int spareBytes) {
		MinecraftDataOutput out = MinecraftDataOutputImpl.create(expectedSize() + spareBytes);
		for (int i = 0; i < spareBytes; i++) {
			out.writeByte(0);
		}
		writeData(out);
		return out.toByteArray();
	}

	@Override
	public final void handleData(byte[] data, int offset) {
		MinecraftDataInput in = MinecraftDataInputImpl.create(data, offset);
		readData(in);
	}

}
