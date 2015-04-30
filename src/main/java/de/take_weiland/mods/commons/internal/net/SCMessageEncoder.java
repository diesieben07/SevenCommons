package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.*;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public final class SCMessageEncoder extends ChannelOutboundHandlerAdapter {

    public static final SCMessageEncoder INSTANCE = new SCMessageEncoder();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof MessageChannelPair) {
            ctx.write(((MessageChannelPair) msg).makeMcPacket(), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
