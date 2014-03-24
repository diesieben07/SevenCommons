package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketResponseHandler;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author diesieben07
 */
public class ResponsePacket extends ModPacket {

	private static final Map<Integer, Object[]> handlers = new ConcurrentHashMap<>();
	private static AtomicInteger nextTransferId = new AtomicInteger();

	public static int nextTransferId() {
		return nextTransferId.getAndIncrement();
	}

	public static <T> void registerHandler(int transferId, WithResponse<T> packet, PacketResponseHandler<? super T> handler) {
		handlers.put(transferId, new Object[] {packet, handler});
	}

	@Override
	protected void handle(DataBuf buffer, EntityPlayer player, Side side) {
		System.out.println("hello!");
		Integer transferId = buffer.getInt();
		Object[] handlerAndPacket = handlers.get(transferId);
		if (handlerAndPacket == null) {
			SevenCommons.LOGGER.warning("Received unknown transferId!");
			return;
		}

		System.out.println("worked! " + Arrays.toString(handlerAndPacket));

		// casts are safe, T of handler and packet must match and ASM generated code
		@SuppressWarnings("unchecked")
		ModPacket.WithResponse<Object> packet = (WithResponse<Object>) handlerAndPacket[0];
		@SuppressWarnings("unchecked")
		ResponseHandlerProxy<Object> handler = (ResponseHandlerProxy<Object>) handlerAndPacket[1];

		Object response = packet.readResponse(buffer, player, side);
		if (handler._sc$handlePacketResponse(response, player)) {
			handlers.remove(transferId);
		}
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
