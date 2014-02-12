package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.logging.Logger;

class SimplePacketHandler<TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> implements PacketHandler<TYPE> {

	private static final SimplePacketHandler<?> INSTANCE;
	
	static {
		INSTANCE = init();
	}
	
	private static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> SimplePacketHandler<?> init() {
		return new SimplePacketHandler<TYPE>();
	}
	
	private static final Logger logger;
	
	static {
		String loggerName = "SC|SimpleNetwork";
		FMLLog.makeLog(loggerName);
		logger = Logger.getLogger(loggerName);
	}
	
	@Override
	public void handle(TYPE t, DataBuf buffer, EntityPlayer player, Side side) {
		try {
			ModPacket<TYPE> packet = t.packet().newInstance();
			if (packet.validOn(side)) {
				packet.handle(buffer, player, side);
			} else {
				logger.warning("Received Packet " + t + " for invalid Side " + side + "!");
				if (side.isServer()) {
					((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Protocol Error (Unexpected Packet)!");
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Invalid Packet class, this should not happen!", e);
		}
	}
	
	@SuppressWarnings("unchecked") // safe, we only call packet()
	static <TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> SimplePacketHandler<TYPE> instance() {
		return (SimplePacketHandler<TYPE>) INSTANCE;
	}

}
