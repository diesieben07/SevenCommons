package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public final class ToClientEncoder extends ChannelOutboundHandlerAdapter {

    private static final int MAX_SIZE = 0x1FFF9A;

    static final ToClientEncoder INSTANCE = new ToClientEncoder();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            byte[] data = ((BaseNettyPacket) msg)._sc$encode();
            String channel = ((BaseNettyPacket) msg)._sc$channel();
            if (data.length > MAX_SIZE) {
                NetworkImpl.writeMultipartPacket(channel, data, ctx, promise, MAX_SIZE, S3FPacketCustomPayload::new);
            } else {
                ctx.write(new S3FPacketCustomPayload(channel, data), promise);
            }
        } else {
            ctx.write(msg, promise);
        }
    }
}
