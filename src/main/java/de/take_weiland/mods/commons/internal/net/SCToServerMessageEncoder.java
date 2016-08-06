package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author diesieben07
 */
public final class SCToServerMessageEncoder extends ChannelOutboundHandlerAdapter {

    public static final SCToServerMessageEncoder INSTANCE = new SCToServerMessageEncoder();

    private SCToServerMessageEncoder() {}

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof InternalPacket) {
            ctx.write(NetworkImpl.toServerVanillaPacket((InternalPacket) msg));
        } else {
            ctx.write(msg, promise);
        }
    }

}
