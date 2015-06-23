package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.RawPacket;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
@ChannelHandler.Sharable
public final class SCMessageHandlerClient extends ChannelInboundHandlerAdapter {

    public static final SCMessageHandlerClient INSTANCE = new SCMessageHandlerClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RawPacket) {
            ((RawPacket) msg).handle(Players.getClient());
        } else if (!(msg instanceof S3FPacketCustomPayload) || !NetworkImpl.handleClientCustomPacket((S3FPacketCustomPayload) msg)) {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Exception in netty pipeline on client!");
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }
}
