package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/net/BaseNettyPacket";
    String HANDLE = "_sc$handle";
    String ENCODE_PLAYER = "_sc$encodeToPlayer";
    String ENCODE = "_sc$encode";

    void _sc$handle(EntityPlayer player);

    net.minecraft.network.Packet _sc$encodeToPlayer(EntityPlayerMP player);

    net.minecraft.network.Packet _sc$encode();

}
