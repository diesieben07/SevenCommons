package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.FMLPacketHandlerImpl;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author diesieben07
 */
public class Packet250FakeNoMP extends Packet250CustomPayload implements SimplePacket {

	private final FMLPacketHandlerImpl handler;

	public Packet250FakeNoMP(FMLPacketHandlerImpl handler, String channel, byte[] bytes, int len) {
		this.handler = handler;
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
		handler.handlePacket(MCDataInputStream.create(data, 0, length), nh.getPlayer());
	}
}
