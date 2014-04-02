package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface PacketResponseHandler {

	void onResponse(DataBuf in, EntityPlayer responder);

}
