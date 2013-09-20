package de.take_weiland.mods.commons.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

class Packet250Transport extends PacketTransportAbstract implements IPacketHandler {

	private static final int PREFIX_COUNT = 1;
	final String channel;
	
	Packet250Transport(String channel, PacketType[] packets) {
		super(packets);
		this.channel = channel;
		NetworkRegistry.instance().registerChannel(this, channel);
	}
	
	@Override
	public Packet toVanilla(ModPacket packet) {
		byte[] data = packet.getData(PREFIX_COUNT);
		data[0] = UnsignedBytes.checkedCast(packet.type().packetId());
		return PacketDispatcher.getPacket(channel, data);
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload vanillaPacket, Player fmlPlayer) {
		byte[] data = vanillaPacket.data;
		int packetId = UnsignedBytes.toInt(data[0]);
		finishPacketRecv(packetId, (EntityPlayer)fmlPlayer, data, PREFIX_COUNT);
	}

	@Override
	public int bytePrefixCount() {
		return PREFIX_COUNT;
	}
	
}