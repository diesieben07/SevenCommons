package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author diesieben07
 */
public class Packet250Fake extends Packet250CustomPayload {

	private final ModPacket modPacket;

	public Packet250Fake(ModPacket modPacket, String channel, byte[] bytes, int len) {
		this.modPacket = modPacket;
		this.channel = channel;
		this.data = bytes;
		this.length = len;
	}

	@Override
	public void writePacketData(DataOutput out) {
		try {
			writeString(channel, out);
			ASMHooks.writeExtPacketLen(out, length);
			out.write(data, 0, length);
		} catch (IOException e) {
			// stupid bug
			throw JavaUtils.throwUnchecked(e);
		}
	}

	@Override
	public void processPacket(NetHandler nh) {
		MCDataInputStream in = MCDataInputStream.create(data, 0, length);
		in.readVarInt(); // skip packet ID
		((ModPacketProxy) modPacket)._sc$handler().handlePacket(in, nh.getPlayer(), modPacket);
	}

	@Override
	public int getPacketSize() {
		return super.getPacketSize();
		// TODO
	}

	@Override
	public void readPacketData(DataInput in) {
		throw new AssertionError();
	}

}
