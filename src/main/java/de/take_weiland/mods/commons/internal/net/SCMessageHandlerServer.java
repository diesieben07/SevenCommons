package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.RawPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @author diesieben07
 */
public final class SCMessageHandlerServer extends ChannelInboundHandlerAdapter {

    private final EntityPlayerMP player;

    public SCMessageHandlerServer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RawPacket) {
            ((RawPacket) msg).handle(player);
        } else if (!(msg instanceof C17PacketCustomPayload) || !NetworkImpl.handleServerCustomPacket((C17PacketCustomPayload) msg, player)) {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Exception in netty pipeline on server!");
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }
}
