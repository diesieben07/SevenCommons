package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface ChannelHandler {

    void accept(String channel, byte[] data, EntityPlayer player, Side side);

    default byte characteristics() {
        return RawPacket.BIDIRECTIONAL;
    }

}
