package de.take_weiland.mods.commons.network;

import java.io.ByteArrayInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

final class Packet250Transport extends PacketTransportAbstract implements IPacketHandler {

	private static final int MAX_SIZE = 32767 - 1;
	private final String channel;
	
	<E extends Enum<E> & PacketType> Packet250Transport(String channel, Class<E> typeClass) {
		super(typeClass);
		this.channel = channel;
		NetworkRegistry.instance().registerChannel(this, channel);
	}
	
	@Override
	public void prepareOutput(DataOutput out, PacketType type) {
		try {
			out.writeByte(UnsignedBytes.checkedCast(type.packetId()));
		} catch (IOException e) {
			throw PacketTransports.wrapIOException(type, e);
		}
	}

	@Override
	public Packet make(byte[] data, PacketType type) {
		return PacketDispatcher.getPacket(channel, data);
	}
	
	@Override
	public Packet make(ModPacket packet) {
		PacketType type = checkNonMulti(packet);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(packet.expectedSize() + 1);
		out.write(UnsignedBytes.checkedCast(type.packetId()));
		writePacket(packet, out);
		return PacketDispatcher.getPacket(channel, out.toByteArray());
	}

	@Override
	public Packet[] makeMulti(ModPacket packet) {
		PacketType type = checkMulti(packet);
		
		final List<byte[]> streams = makeMultiparts(packet, new byte[] { UnsignedBytes.checkedCast(type.packetId()) });
		
		int numPackets = streams.size();
		Packet[] packets = new Packet[numPackets];
		for (int i = 0; i < numPackets; ++i) {
			byte[] data = streams.get(i);
			data[2] = UnsignedBytes.checkedCast(numPackets);
			
			packets[i] = PacketDispatcher.getPacket(channel, data);
		}
		
		return packets;
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload vanillaPacket, Player fmlPlayer) {
		ByteArrayInputStream in = new ByteArrayInputStream(vanillaPacket.data);
		int packetId = in.read();
		finishPacketRecv(manager, packetId, (EntityPlayer)fmlPlayer, in);
	}
	
	@Override
	public int maxPacketSize() {
		return MAX_SIZE;
	}

}