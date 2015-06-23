package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.RawPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.entity.player.EntityPlayerMP;

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
        if (msg instanceof RawPacket) {
            ctx.write(((RawPacket) msg).encodeToPlayer(player), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
