package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>a handler for your custom packets. Create a {@link de.take_weiland.mods.commons.net.PacketFactory} using your handler with
 * {@link de.take_weiland.mods.commons.net.Network#makeFactory(String, Class, PacketHandler)}</p>
 */
public interface PacketHandler<TYPE extends Enum<TYPE>> {

	/**
	 * handle the given packet type on the given side with the given player. Read the data from the given buffer.
	 *
	 * @param t      the Packet type being received
	 * @param buffer the buffer to read your data from
	 * @param player the player handling this packet, on the client side it's the client-player, on the server side it's the player sending the packet
	 * @param side   the (logical) side receiving the packet
	 */
	void handle(TYPE t, DataBuf in, EntityPlayer player, Side side);

}
