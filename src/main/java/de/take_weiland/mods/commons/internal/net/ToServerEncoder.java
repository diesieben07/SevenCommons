package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author diesieben07
 */
public final class ToServerEncoder extends ChannelOutboundHandlerAdapter {

    public static final ChannelHandler INSTANCE = new ToServerEncoder();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof PacketCodecPair) {
            ctx.write(((PacketCodecPair<?>) msg).makeServerboundMCPacket(), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
