package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
final class ClientInboundHandler extends ChannelInboundHandlerAdapter {

    static final ClientInboundHandler instance = new ClientInboundHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof S3FPacketCustomPayload) || !NetworkImpl.handleClientCustomPacket((S3FPacketCustomPayload) msg)) {
            ctx.fireChannelRead(msg);
        }
    }
}
