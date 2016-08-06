package de.take_weiland.mods.commons.internal.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetworkManager;

/**
 * <p>A handler for raw custom payload messages.</p>
 *
 * @author diesieben07
 */
public interface ChannelHandler {

    /**
     * <p>Called when a custom payload message is received on the given channel.</p>
     * @param channel the channel
     * @param data    the payload
     * @param side the side
     * @param manager  the network manager
     */
    void accept(String channel, ByteBuf data, byte side, NetworkManager manager);

}
