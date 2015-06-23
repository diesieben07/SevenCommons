package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    void _sc$handle(EntityPlayer player);

    net.minecraft.network.Packet _sc$encodeToPlayer(EntityPlayerMP player);

    net.minecraft.network.Packet _sc$encode();

}
