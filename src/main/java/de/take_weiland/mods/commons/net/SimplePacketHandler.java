package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

final class SimplePacketHandler<TYPE extends Enum<TYPE> & SimplePacketType> implements PacketHandler<TYPE> {

	private static final SimplePacketHandler<?> INSTANCE;

	static {
		INSTANCE = createInstance();
	}

	private static SimplePacketHandler<? extends Enum<?>> createInstance() {
		return new SimplePacketHandler<>();
	}

	@Override
	public void handle(TYPE t, DataBuf buffer, EntityPlayer player, Side side) {
		try {
			ModPacketBase packet = t.packet().newInstance();
			if (packet.validOn(side)) {
				packet.handle0(buffer, player, side);
			} else {
				Network.logger.warning("Received Packet " + t + " for invalid Side " + side + "!");
				if (side.isServer()) {
					((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Protocol Error (Unexpected Packet)!");
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Invalid Packet class, this should not happen!", e);
		}
	}
	
	@SuppressWarnings("unchecked") // safe, we can handle any TYPE
	static <TYPE extends Enum<TYPE> & SimplePacketType> SimplePacketHandler<TYPE> instance() {
		return (SimplePacketHandler<TYPE>) INSTANCE;
	}

}
