package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.ModPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;

/**
 * @author diesieben07
 */
public interface PacketHandlerProxy {

	Packet buildPacket(ModPacket packet);

	void handlePacket(MCDataInputStream in, EntityPlayer player, ModPacket packet);

	void handlePacket(MCDataInputStream in, EntityPlayer player);

}
