package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public class NettyBlob<P> {

    private final String channel;
    private final P packet;
    private final BiConsumer<? super P, ? super EntityPlayer> handler;
    private final BiFunction<? super P, ? super EntityPlayer, ? extends byte[]> encoder;

    public NettyBlob(String channel, P packet, BiConsumer<? super P, ? super EntityPlayer> handler, BiFunction<? super P, ? super EntityPlayer, ? extends byte[]> encoder) {
        this.channel = channel;
        this.packet = packet;
        this.handler = handler;
        this.encoder = encoder;
    }
}
