package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>A handler for raw custom payload messages.</p>
 * <p>Register using {@link Network#registerHandler(String, ChannelHandler)}.</p>
 *
 * @author diesieben07
 */
public interface ChannelHandler {

    /**
     * <p>Called when a custom payload message is received on the given channel.</p>
     *
     * @param channel the channel
     * @param data    the payload
     * @param player  the player
     * @param side    the logical side
     */
    void accept(String channel, byte[] data, EntityPlayer player, Side side);

    /**
     * <p>A bitmap describing characteristics for this handler.</p>
     * <p>This may be a bitwise-or combination of one or more of {@link Network#ASYNC}, {@link Network#CLIENT}
     * and {@link Network#SERVER}.</p>
     * <p>The default value is {@link Network#BIDIRECTIONAL}.</p>
     *
     * @return the characteristics for this handler
     */
    default byte characteristics() {
        return Network.BIDIRECTIONAL;
    }

}
