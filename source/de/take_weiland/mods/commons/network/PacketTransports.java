package de.take_weiland.mods.commons.network;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.FMLNetworkHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.NetworkModHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.CollectionUtils;
import de.take_weiland.mods.commons.util.Sides;
import de.take_weiland.mods.commons.util.UnsignedShorts;

public final class PacketTransports {

	private PacketTransports() { }
	
	public static PacketTransport withPacket250(String channel, PacketType[] packets) {
		return new Packet250Transport(channel, packets);
	}
	
	public static PacketTransport withPacket131(Object mod, PacketType[] packets) {
		return new Packet131Transport(mod, packets);
	}
	
	private abstract static class AbstractPacketTransport implements PacketTransport {
		
		private final Class<? extends ModPacket>[] packets;

		protected AbstractPacketTransport(PacketType[] types) {
			@SuppressWarnings("unchecked")
			Class<? extends ModPacket>[] packets = new Class[types.length];
			for (PacketType type : types) {
				int packetId = type.packetId();
				if (!CollectionUtils.arrayIndexExists(packets, packetId)) {
					packets = Arrays.copyOf(packets, packetId + 1);
				}
				packets[packetId] = type.packetClass();
			}
			this.packets = packets;
		}
		
		protected final ModPacket newPacket(int packetId) {
			Class<? extends ModPacket> packetClass = CollectionUtils.safeArrayAccess(packets, packetId);
			if (packetClass == null) {
				throw new NetworkException(String.format("Unknown PacketId: %d", Integer.valueOf(packetId)));
			}
			
			try {
				return packetClass.newInstance();
			} catch (ReflectiveOperationException e) {
				throw new NetworkException(String.format("Failed to instantiate Packet class %s", packetClass.getName()), e);
			}
		}
		
		protected final void finishPacket(int packetId, EntityPlayer player, byte[] data, int dataOffset) {
			ModPacket packet = newPacket(packetId);
			packet.handleData(data, dataOffset);
			Side side = Sides.logical(player);
			if (!packet.isValidForSide(side)) {
				throw new NetworkException(String.format("Received Packet %s for invalid side %s", packet.getClass().getName(), side));
			}
			packet.execute(player, side);
		}
		
	}

	private static class Packet250Transport extends AbstractPacketTransport implements IPacketHandler {

		private final String channel;
		
		Packet250Transport(String channel, PacketType[] packets) {
			super(packets);
			this.channel = channel;
			NetworkRegistry.instance().registerChannel(this, channel);
		}
		
		@Override
		public Packet toVanilla(ModPacket packet) {
			byte[] data = packet.getData(1);
			data[0] = UnsignedBytes.checkedCast(packet.type().packetId());
			return PacketDispatcher.getPacket(channel, data);
		}
		
		@Override
		public void onPacketData(INetworkManager manager, Packet250CustomPayload vanillaPacket, Player fmlPlayer) {
			byte[] data = vanillaPacket.data;
			int packetId = UnsignedBytes.toInt(data[0]);
			finishPacket(packetId, (EntityPlayer)fmlPlayer, data, 1);
		}
		
	}
	
	private static class Packet131Transport extends AbstractPacketTransport implements ITinyPacketHandler {

		private final Object mod;
		
		Packet131Transport(Object mod, PacketType[] packets) {
			super(packets);
			this.mod = mod;
			NetworkModHandler nmh = FMLNetworkHandler.instance().findNetworkModHandler(mod);
			ReflectionHelper.setPrivateValue(NetworkModHandler.class, nmh, this, "tinyPacketHandler");
		}
		
		@Override
		public Packet toVanilla(ModPacket packet) {
			return PacketDispatcher.getTinyPacket(mod, UnsignedShorts.checkedCast(packet.type().packetId()), packet.getData(0));
		}

		@Override
		public void handle(NetHandler handler, Packet131MapData mapData) {
			finishPacket(UnsignedShorts.toInt(mapData.uniqueID), handler.getPlayer(), mapData.itemData, 0);
		}

	}
}
