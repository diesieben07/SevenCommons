package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface ResponseHandlerProxy<T> {

	public static final String HANDLE = "_sc$handlePacketResponse";
	public static final String ADD_COUNT = "_sc$addReceiverCount";

	boolean _sc$handlePacketResponse(T response, EntityPlayer responder);

	void _sc$addReceiverCount(int amount);

}
