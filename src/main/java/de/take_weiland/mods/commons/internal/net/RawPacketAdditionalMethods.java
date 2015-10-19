package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.RawPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface RawPacketAdditionalMethods extends BaseNettyPacket {

    String CLASS_NAME = "de/take_weiland/mods/commons/net/RawPacketAdditionalMethods";

    @Override
    default byte _sc$characteristics() {
        return ((RawPacket) this).characteristics();
    }

    @Override
    default void _sc$handle(EntityPlayer player) {
        ((RawPacket) this).handle(player);
    }

    @Override
    default String _sc$channel() {
        return ((RawPacket) this).channel();
    }

    @Override
    default byte[] _sc$encode() {
        return ((RawPacket) this).encode();
    }

    @Override
    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return ((RawPacket) this).encodeToPlayer(player);
    }
}
