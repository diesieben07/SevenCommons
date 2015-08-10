package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * @author diesieben07
 */
public interface RawPacket extends BaseNettyPacket {

    void handle(EntityPlayer player);

    byte[] encode();

    default byte[] encodeToPlayer(EntityPlayerMP player) {
        return encode();
    }

    @Override
    default void _sc$handle(EntityPlayer player) {
        handle(player);
    }

    @Override
    default byte[] _sc$encode() {
        return encode();
    }

    @Override
    default byte[] _sc$encodeToPlayer(EntityPlayerMP player) {
        return encodeToPlayer(player);
    }

}
