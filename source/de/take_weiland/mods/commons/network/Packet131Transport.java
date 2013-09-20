package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.ReflectionHelper;
import de.take_weiland.mods.commons.util.UnsignedShorts;

class Packet131Transport extends PacketTransportAbstract implements ITinyPacketHandler {

	private static final int PREFIX_COUNT = 0;
	private final Object mod;
	
	Packet131Transport(Object mod, PacketType[] packets) {
		super(packets);
		this.mod = mod;
		NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mod);
		ReflectionHelper.setPrivateValue(NetworkModHandler.class, nmh, this, "tinyPacketHandler");
	}
	
	@Override
	public Packet toVanilla(ModPacket packet) {
		return PacketDispatcher.getTinyPacket(mod, UnsignedShorts.checkedCast(packet.type().packetId()), packet.getData(PREFIX_COUNT));
	}

	@Override
	public void handle(NetHandler handler, Packet131MapData mapData) {
		finishPacketRecv(UnsignedShorts.toInt(mapData.uniqueID), handler.getPlayer(), mapData.itemData, PREFIX_COUNT);
	}

	@Override
	public int bytePrefixCount() {
		return PREFIX_COUNT;
	}

}