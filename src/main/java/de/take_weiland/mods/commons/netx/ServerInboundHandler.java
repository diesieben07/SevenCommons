package de.take_weiland.mods.commons.netx;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @author diesieben07
 */
final class ServerInboundHandler extends ChannelInboundHandlerAdapter {

    private final EntityPlayerMP player;

    ServerInboundHandler(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof C17PacketCustomPayload) || !NetworkImpl.handleServerCustomPacket(((C17PacketCustomPayload) msg), player)) {
            ctx.fireChannelRead(msg);
        }
    }
}
