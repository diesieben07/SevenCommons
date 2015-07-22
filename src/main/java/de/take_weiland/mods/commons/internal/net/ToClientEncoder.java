package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
public final class ToClientEncoder extends ChannelOutboundHandlerAdapter {

    private final EntityPlayerMP player;

    public ToClientEncoder(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            byte[] data = ((BaseNettyPacket) msg)._sc$encodeToPlayer(player);
            String channel = ((BaseNettyPacket) msg)._sc$channel();
            ctx.write(new S3FPacketCustomPayload(channel, data), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
