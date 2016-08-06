package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public class SCToClientMessageEncoder extends ChannelOutboundHandlerAdapter {

    public static final SCToClientMessageEncoder INSTANCE = new SCToClientMessageEncoder();

    private SCToClientMessageEncoder() {}

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof InternalPacket) {
            ctx.write(NetworkImpl.toClientVanillaPacket((InternalPacket) msg));
        } else {
            ctx.write(msg, promise);
        }
    }
}
