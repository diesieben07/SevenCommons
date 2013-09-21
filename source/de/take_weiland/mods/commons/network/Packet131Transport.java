package de.take_weiland.mods.commons.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.ReflectionHelper;
import de.take_weiland.mods.commons.util.UnsignedShorts;

class Packet131Transport extends PacketTransportAbstract implements ITinyPacketHandler {

	private final Object mod;
	
	Packet131Transport(Object mod, PacketType[] packets) {
		super(packets);
		this.mod = mod;
		NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mod);
		ReflectionHelper.setPrivateValue(NetworkModHandler.class, nmh, this, "tinyPacketHandler");
	}
	
	@Override
	public Packet make(ModPacket packet) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(packet.expectedSize());
		writePacket(packet, out);
		return PacketDispatcher.getTinyPacket(mod, UnsignedShorts.checkedCast(packet.type().packetId()), out.toByteArray());
	}
	
	@Override
	public Packet[] makeMulti(MultipartPacket packet) {
		short packetId = UnsignedShorts.checkedCast(packet.type().packetId());
		
		final List<byte[]> chunks = makeMultiparts(packet, ArrayUtils.EMPTY_BYTE_ARRAY);
		
		int numPackets = chunks.size();
		Packet[] packets = new Packet[numPackets];
		
		for (int i = 0; i < numPackets; ++i) {
			byte[] data = chunks.get(i);
			data[1] = UnsignedBytes.checkedCast(numPackets);
			
			packets[i] = PacketDispatcher.getTinyPacket(mod, packetId, data);
		}
		
		return packets;
	}

	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) {
		ByteArrayInputStream in = new ByteArrayInputStream(mapData.itemData);
		finishPacketRecv(Packets.getNetworkManager(handler), UnsignedShorts.toInt(mapData.uniqueID), handler.getPlayer(), in);
	}

	@Override
	public int maxPacketSize() {
		return UnsignedShorts.MAX_VALUE;
	}

}