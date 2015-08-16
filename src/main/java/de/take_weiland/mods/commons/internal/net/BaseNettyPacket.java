package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.RawPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
public interface BaseNettyPacket {

    static byte encodeCharacteristics(Side side, boolean async) {
        return (byte) ((side == null ? RawPacket.BIDIRECTIONAL : side == Side.CLIENT ? RawPacket.CLIENT : RawPacket.SERVER) | (async ? RawPacket.ASYNC : 0));
    }

    static byte sideToCode(@Nonnull Side side) {
        return side == Side.CLIENT ? RawPacket.CLIENT : RawPacket.SERVER;
    }

    void _sc$handle(EntityPlayer player);

    String _sc$channel();

    byte[] _sc$encode();

    default byte _sc$characteristics() {
        return RawPacket.BIDIRECTIONAL;
    }

    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return _sc$encode();
    }

}
