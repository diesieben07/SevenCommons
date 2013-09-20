package de.take_weiland.mods.commons.network;

import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public abstract class StreamPacket extends AbstractModPacket {

	protected abstract void writeData(MinecraftDataOutput out);
	
	protected abstract void readData(MinecraftDataInput in);
	
	protected int expectedSize() {
		return 32;
	}
	
	@Override
	public final byte[] getData(int offset) {
		MinecraftDataOutput out = MinecraftDataOutput.create(expectedSize() + offset);
		out.skip(offset);
		writeData(out);
		return out.toNewArray();
	}

	@Override
	public final void handleData(byte[] data, int offset) {
		MinecraftDataInput in = MinecraftDataInput.create(data, offset, data.length);
		readData(in);
	}

}
