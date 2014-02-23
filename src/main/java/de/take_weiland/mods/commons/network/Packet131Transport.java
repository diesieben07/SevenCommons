package de.take_weiland.mods.commons.network;

import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.ReflectionHelper;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.DataOutput;
import java.util.List;

class Packet131Transport extends PacketTransportAbstract implements ITinyPacketHandler {

	private final Object mod;
	
	<E extends Enum<E> & PacketType> Packet131Transport(Object mod, Class<E> typeClass) {
		super(typeClass);
		this.mod = mod;
		NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mod);
		ReflectionHelper.setPrivateValue(NetworkModHandler.class, nmh, this, "tinyPacketHandler");
	}
	
	@Override
	public void prepareOutput(DataOutput out, PacketType type) {
		// nothing to do
	}

	@Override
	public Packet make(byte[] data, PacketType type) {
		return PacketDispatcher.getTinyPacket(mod, UnsignedShorts.checkedCast(type.packetId()), data);
	}
	
	@Override
	public Packet make(ModPacket packet) {
		PacketType type = checkNonMulti(packet);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(packet.expectedSize());
		writePacket(packet, out);
		return PacketDispatcher.getTinyPacket(mod, UnsignedShorts.checkedCast(type.packetId()), out.toByteArray());
	}
	
	@Override
	public Packet[] makeMulti(ModPacket packet) {
		PacketType type = checkMulti(packet);
		
		short packetId = UnsignedShorts.checkedCast(type.packetId());
		
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
		finishPacketRecv(Network.getNetworkManager(handler), UnsignedShorts.toInt(mapData.uniqueID), handler.getPlayer(), in);
	}

	@Override
	public int maxPacketSize() {
		return UnsignedShorts.MAX_VALUE;
	}

}