package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface PacketResponseHandler<T extends ModPacket.Response> {

	void onResponse(T packet, EntityPlayer responder);

}
