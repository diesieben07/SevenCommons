package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
public final class ToServerEncoder extends ChannelOutboundHandlerAdapter {

    public static final ChannelHandler INSTANCE = new ToServerEncoder();
    private static final int MAX_SIZE = 32766;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            byte[] data = ((BaseNettyPacket) msg)._sc$encode();
            String channel = ((BaseNettyPacket) msg)._sc$channel();
            if (data.length > MAX_SIZE) {
                NetworkImpl.writeMultipartPacket(channel, data, ctx, promise, MAX_SIZE, C17PacketCustomPayload::new);
            } else {
                ctx.write(new C17PacketCustomPayload(channel, data), promise);
            }
        } else {
            ctx.write(msg, promise);
        }
    }
}
