package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author diesieben07
 */
public class Packet250Fake extends Packet250CustomPayload {

	private final ModPacket modPacket;
	private final int off;

	public Packet250Fake(ModPacket modPacket, String channel, byte[] bytes, int off, int len) {
		this.modPacket = modPacket;
		this.channel = channel;
		this.data = bytes;
		this.off = off;
		this.length = len;
	}

	@Override
	public void writePacketData(DataOutput out) {
		try {
			writeString(channel, out);
			ASMHooks.writeExtPacketLen(out, length);
			out.write(data, off, length);
		} catch (IOException e) {
			// stupid bug
			throw JavaUtils.throwUnchecked(e);
		}
	}

	@Override
	public void processPacket(NetHandler nh) {
		MCDataInputStream in = MCDataInputStream.create(data, off, length);
		in.readVarInt(); // skip packet ID
		((ModPacketProxy) modPacket)._sc$handler().handlePacket(in, nh.getPlayer(), modPacket);
	}
}
