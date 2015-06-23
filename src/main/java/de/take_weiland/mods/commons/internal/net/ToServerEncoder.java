package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.RawPacket;
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
        if (msg instanceof RawPacket) {
            ctx.write(((RawPacket) msg).encode(), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
