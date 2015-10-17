package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * <p>A low-level packet. It must be handled by a {@link ChannelHandler} on the receiving end.</p>
 * <p>Usually the {@link Packet} interface is a better and more convenient choice.</p>
 *
 * @author diesieben07
 */
public interface RawPacket {

    /**
     * <p>Specifies that this packet may be received on the server-side.</p>
     */
    byte SERVER = 0b0001;
    /**
     * <p>Specifies that this packet may be received on the client-side.</p>
     */
    byte CLIENT = 0b0010;
    /**
     * <p>Specifies that this packet may be received on both sides. This is equivalent to {@code CLIENT|SERVER}.</p>
     */
    byte BIDIRECTIONAL = SERVER | CLIENT;
    /**
     * <p>Specifies that this packet may be handled directly on the netty thread instead of the main game thread for the
     * receiving side.</p>
     */
    byte ASYNC = 0b0100;

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
     * <p>Encode this packet into bytes when sent from server to a client. This method defaults to {@link #encode()}.</p>
     *
     * @param player the player
     * @return the bytes
     */
    default byte[] encodeToPlayer(EntityPlayerMP player) {
        return encode();
    }

    /**
     * <p>A bitmask of characteristics of how this packet should be handled when received directly. This does <i>not</i> apply
     * to any corresponding {@link ChannelHandler}.</p>
     *
     * <p>A combination of the values {@link #SERVER}, {@link #CLIENT} and {@link #ASYNC} may be returned here via the {@code |} operator.</p>
     * <p>The default value is {@code BIDIRECTIONAL}.</p>
     *
     * @return a bitmask of characteristics
     */
    default byte characteristics() {
        return BIDIRECTIONAL;
    }
}
