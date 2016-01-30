package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * <p>A low-level packet. It must be handled by a {@link ChannelHandler} on the receiving end.</p>
 * <p>Usually the {@link Packet} interface is a better and more convenient choice.</p>
 *
 * @author diesieben07
 */
public interface RawPacket extends SimplePacket {

    /**
     * <p>Called when this packet is received directly (only happens on local connections).</p>
     *
     * @param player the player
     */
    void handle(EntityPlayer player);

    /**
     * <p>Encode this packet into bytes.</p>
     *
     * @return the bytes
     */
    byte[] encode();

    /**
     * <p>The channel to send this packet on.</p>
     *
     * @return the channel
     */
    String channel();

    /**
     * <p>A bitmap describing characteristics for this packet.</p>
     * <p>This may be a bitwise-or combination of one or more of {@link Network#ASYNC}, {@link Network#CLIENT}
     * and {@link Network#SERVER}.</p>
     * <p>The default value is just {@link Network#BIDIRECTIONAL}.</p>
     * <p>These settings do <i>not</i> apply to any corresponding {@link ChannelHandler}. These must be set separately
     * using {@link ChannelHandler#characteristics()}.</p>
     *
     * @return the characteristics for this packet
     */
    default byte characteristics() {
        return Network.BIDIRECTIONAL;
    }

    @Override
    default void sendToServer() {
        NetworkImpl.sendRawPacketToServer((BaseNettyPacket) this);
    }

    @Override
    default void sendTo(EntityPlayerMP player) {
        NetworkImpl.sendRawPacket(player, (BaseNettyPacket) this);
    }

}
