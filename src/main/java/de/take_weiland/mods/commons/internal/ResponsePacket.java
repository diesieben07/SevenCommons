package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketResponseHandler;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

/**
 * @author diesieben07
 */
public class ResponsePacket extends ModPacket {

	private static final Map<Integer, Object[]> handlers = Maps.newHashMap();

	@Override
	protected void handle(DataBuf buffer, EntityPlayer player, Side side) {
		int transferId = buffer.getInt();
		Object[] handlerAndPacket = handlers.remove(transferId);
		if (handlerAndPacket == null) {
			SevenCommons.LOGGER.warning("Received unknown transferId!");
		}

		// casts are safe, T of handler and packet must match
		@SuppressWarnings("unchecked")
		ModPacket.WithResponse<Object> packet = (WithResponse<Object>) handlerAndPacket[0];
		@SuppressWarnings("unchecked")
		PacketResponseHandler<Object> handler = (PacketResponseHandler<Object>) handlerAndPacket[1];

		Object response = packet.readResponse(buffer, player, side);
		handler.onResponse(response, player);

	}

	@Override
	protected void write(WritableDataBuf buffer) {
		// doesn't happen, only send via ModPacket.WithResponse
		throw new AssertionError();
	}

	@Override
	protected boolean validOn(Side side) {
		return true;
	}

}
