package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.Network;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    static byte encodeCharacteristics(Side side, boolean async) {
        return (byte) ((side == null ? Network.BIDIRECTIONAL : side == Side.CLIENT ? Network.CLIENT : Network.SERVER) | (async ? Network.ASYNC : 0));
    }

    static byte sideToCode(@Nonnull Side side) {
        return side == Side.CLIENT ? Network.CLIENT : Network.SERVER;
    }

    void _sc$handle(EntityPlayer player);

    String _sc$channel();

    byte[] _sc$encode();

    default byte _sc$characteristics() {
        return Network.BIDIRECTIONAL;
    }

    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return _sc$encode();
    }

}
