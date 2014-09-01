package de.take_weiland.mods.commons.net;

import com.google.common.collect.BiMap;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.Packet250Fake;
import de.take_weiland.mods.commons.internal.Packet250FakeNoMP;
import de.take_weiland.mods.commons.internal.PacketHandlerProxy;
import de.take_weiland.mods.commons.util.SCReflector;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FMLPacketHandlerImpl implements IPacketHandler, PacketHandlerProxy, PacketHandler {

	private final String channel;
	private final Logger logger;
	private final BiMap<Integer, Class<? extends ModPacket>> packets;

	FMLPacketHandlerImpl(String channel, BiMap<Integer, Class<? extends ModPacket>> packets) {
		this.channel = channel;
		this.packets = packets;
		String logChannel = "SCNet|" + channel;
		FMLLog.makeLog(logChannel);
		logger = Logger.getLogger(logChannel);
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fmlPlayer) {
		MCDataInputStream in = MCDataInputStream.create(packet.data, 0, packet.length); // explicitly refer to the size, for memory connections that pass the fake packets
		EntityPlayer player = (EntityPlayer) fmlPlayer;

		int id = in.readVarInt();

		ModPacket modPacket = newPacket(id);
		handlePacket(in, player, modPacket);
	}

	@Override
	public void handlePacket(MCDataInputStream in, EntityPlayer player) {
		handlePacket(in, player, newPacket(in.readVarInt()));
	}

	@Override
	public void handlePacket(MCDataInputStream in, EntityPlayer player, ModPacket modPacket) {
		Side side = Sides.logical(player);
		try {
			if (!((ModPacketProxy) modPacket)._sc$canSideReceive(side)) {
				throw new ProtocolException(String.format("Packet received on wrong Side!"));
			}
			modPacket.read(in, player, side);
			modPacket.execute(player, side);
		} catch (ProtocolException pe) {
			if (pe.playerKickMsg != null && side.isServer()) {
				((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer(pe.playerKickMsg);
			}
			logException(modPacket, pe, player);
		} catch (IOException e) {
			logException(modPacket, e, player);
		}
	}

	@Override
	public Packet buildPacket(ModPacket mp) {
		int id = packets.inverse().get(mp.getClass());
		MCDataOutputStream out = MCDataOutputStream.create(mp.expectedSize() + 1); // packetID should rarely take more than one byte (more than 127)
		out.writeVarInt(id);
		mp.write(out);
		out.lock();
		return new Packet250Fake(mp, channel, out.backingArray(), out.length());
	}

	@Override
	public MCDataOutputStream createStream(int packetId) {
		return createStream(packetId, MCDataOutputStream.INITIAL_CAP);
	}

	@Override
	public MCDataOutputStream createStream(int packetId, int initialCapacity) {
		MCDataOutputStream stream = MCDataOutputStream.create(initialCapacity + 1);
		stream.writeVarInt(packetId);
		return stream;
	}

	@Override
	public SimplePacket makePacket(MCDataOutputStream stream) {
		stream.lock();
		return new Packet250FakeNoMP(this, channel, stream.backingArray(), stream.length());
	}

	private void logException(ModPacket packet, Exception e, EntityPlayer player) {
		logger.log(Level.WARNING, String.format("Unhandled %s during Packet read of Packet %s for player %s", e.getClass().getSimpleName(), packet.getClass().getSimpleName(), player.username), e);
	}

	private ModPacket newPacket(int id) {
		try {
			return packets.get(id).newInstance();
		} catch (ReflectiveOperationException e) {
			// should have been caught by PacketHandlerBuilder already
			throw new AssertionError(e);
		}
	}

	static {
		Map<Class<? extends Packet>, Integer> classToIdMap = SCReflector.instance.getClassToIdMap(null);
		classToIdMap.put(Packet250Fake.class, 250);
		classToIdMap.put(Packet250FakeNoMP.class, 250);
	}
}