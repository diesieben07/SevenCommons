package de.take_weiland.mods.commons.network;

import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.Sides;

public final class ModPacketHandler implements IPacketHandler {

	public static final void setupNetworking(Object mod, PacketType[] packets) {
		new ModPacketHandler(mod, packets);
	}
	
	private final Map<Byte, PacketType> packets;
	
	private ModPacketHandler(Object mod, PacketType[] packets) {
		Set<String> channels = Sets.newHashSetWithExpectedSize(1);
		
		ImmutableMap.Builder<Byte, PacketType> builder = ImmutableMap.builder();
		for (PacketType packet : packets) {
			builder.put(Byte.valueOf(packet.getPacketId()), packet);
			channels.add(packet.getChannel());
		}
		this.packets = builder.build();
		
		for (String channel : channels) {
			NetworkRegistry.instance().registerChannel(this, channel);
		}
		
		SevenCommons.LOGGER.info(String.format("Registered ModPacketHandler for %s", mod.getClass().getSimpleName()));
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		byte[] data = packet.data;
		Byte packetId = Byte.valueOf(data[0]);

		try {
			PacketType type = packets.get(packetId);
			if (type == null) {
				throw new NetworkException(String.format("Unknown packetId: %s", packetId));
			}
		
			handleReceivedPacket(type, data, (EntityPlayer)player);
		} catch (Exception t) {
			SevenCommons.LOGGER.severe(String.format("Exception during packet handling for player %s with channel %s", ((EntityPlayer)player).username, packet.channel));
			t.printStackTrace();
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer("Protocol Error!");
			}
		}
	}
	
	private final void handleReceivedPacket(PacketType type, byte[] data, EntityPlayer player) throws ReflectiveOperationException {
		AbstractModPacket mp = type.getPacketClass().newInstance();
		Side side = Sides.logical(player);
		if (!mp.isValidForSide(side)) {
			throw new NetworkException("Packet " + mp.getClass().getSimpleName() + " received for invalid side " + side);
		}
		mp.readData(data);
		mp.execute(player, side);
	}
}
