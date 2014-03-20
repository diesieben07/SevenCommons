package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface PacketResponseHandler<T> {

	/**
	 * handle a packet response
	 * @param response the received response
	 * @param responder the player responding, null if the response comes from the server
	 */
	void onResponse(T response, EntityPlayer responder);

}
