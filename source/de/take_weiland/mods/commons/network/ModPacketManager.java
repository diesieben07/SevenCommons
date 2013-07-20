package de.take_weiland.mods.commons.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet131MapData;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.ModdingUtils;

public class ModPacketManager implements IPacketHandler, ITinyPacketHandler {

	private final BiMap<Integer, Class<? extends ModPacket>> packetTypes;
	private final String channel;
	
	protected ModPacketManager() {
		CommonsNetworkMod nm = getNetworkMod(Loader.instance().activeModContainer());
		packetTypes = ImmutableBiMap.copyOf(nm.getPacketList());
		channel = nm.getChannel();
	}
	
	private static final CommonsNetworkMod getNetworkMod(ModContainer mc) {
		if (mc instanceof CommonsNetworkMod) {
			return (CommonsNetworkMod) mc;
		} else if (mc.getMod() instanceof CommonsNetworkMod) {
			return (CommonsNetworkMod) mc.getMod();
		} else {
			throw new IllegalArgumentException("Mod " + mc.getModId() + " has to implement CommonsNetworkMod");
		}
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(channel)) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			Integer packetId = Integer.valueOf(data.readUnsignedByte());
			handleReceivedPacket(packetId, data, (EntityPlayer)player);
		}
	}

	@Override
	public void handle(NetHandler handler, Packet131MapData packet) {
		handleReceivedPacket(Integer.valueOf(packet.uniqueID), ByteStreams.newDataInput(packet.itemData), handler.getPlayer());
	}
	
	private final void handleReceivedPacket(Integer packetId, ByteArrayDataInput data, EntityPlayer player) {
		Class<? extends ModPacket> packetClass = packetTypes.get(packetId);
		
		if (packetClass == null) {
			throw new NetworkException("Invalid packetId: " + packetId);
		}
		
		try {
			ModPacket mp = packetClass.newInstance();
			
			Side side = ModdingUtils.determineSide(player);
			if (!mp.isValidForSide(side)) {
				if (side.isServer()) {
					((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer("Invalid Packet!");
				}
				throw new NetworkException("Packet " + packetClass.getName() + " received for invalid side " + side);
			}
			
			
			mp.readData(data);
			mp.execute(player, side);
		} catch (ReflectiveOperationException e) {
			throw new NetworkException("Invalid Packet class " + packetClass.getSimpleName(), e);
		} catch (Throwable t) {
			Throwables.propagateIfPossible(t);
			throw new NetworkException("Exception duing packet handling!", t);
		}
	}
}
