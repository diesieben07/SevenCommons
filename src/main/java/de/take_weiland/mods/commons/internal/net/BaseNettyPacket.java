package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    void _sc$handle(EntityPlayer player);

    String _sc$channel();

    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return _sc$encode();
    }

    byte[] _sc$encode();

}
