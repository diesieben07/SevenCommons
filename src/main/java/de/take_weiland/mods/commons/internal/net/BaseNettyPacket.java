package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    void _sc$handle(EntityPlayer player);

    String _sc$channel();

    byte[] _sc$encode();

    default boolean _sc$canReceive(Side side) {
        return true;
    }

    default boolean _sc$async() {
        return false;
    }

    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return _sc$encode();
    }

}
