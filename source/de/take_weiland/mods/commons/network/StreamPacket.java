package de.take_weiland.mods.commons.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public abstract class StreamPacket extends AbstractModPacket {

	/**
	 * reads this packet's data from the stream
	 * @param in
	 */
	protected abstract void readData(ByteArrayDataInput in);
	
	/**
	 * writes this packet's data to the stream
	 * @param out
	 */
	protected abstract void writeData(ByteArrayDataOutput out);
	
	protected int getExpectedSize() {
		return 32;
	}

	@Override
	protected final byte[] writeData() {
		ByteArrayDataOutput out = ByteStreams.newDataOutput(getExpectedSize());
		out.writeByte(getType().getPacketId());
		writeData(out);
		return out.toByteArray();
	}

	@Override
	protected void readData(byte[] data) {
		ByteArrayDataInput in = ByteStreams.newDataInput(data, 1); // we don't want the packetId
		readData(in);
	}
	
}
