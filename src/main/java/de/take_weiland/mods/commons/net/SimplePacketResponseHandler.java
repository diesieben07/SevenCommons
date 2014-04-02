package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface SimplePacketResponseHandler<T> {

	/**
	 * handle a packet createResponse
	 * @param response the received createResponse
	 * @param responder the player responding, null if the response comes from the server
	 */
	void onResponse(T response, EntityPlayer responder);

}
