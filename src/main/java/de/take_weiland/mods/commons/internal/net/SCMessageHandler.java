package de.take_weiland.mods.commons.internal.net;

import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.ProtocolException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.ChatComponentText;

/**
 * @author diesieben07
 */
abstract class SCMessageHandler extends ChannelInboundHandlerAdapter {

    abstract Side side();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ProtocolException) {
            ProtocolException pe = (ProtocolException) cause;
            if (pe.getKickMessage() != null) {
                ctx.attr(NetworkDispatcher.FML_DISPATCHER).get().manager.closeChannel(new ChatComponentText(pe.getKickMessage()));
            }
            NetworkImpl.LOGGER.warn("ProtocolException on " + (side().name().toLowerCase()) + "-side netty channel", pe);
        }
        super.exceptionCaught(ctx, cause);
    }

    byte[] multipartData;
    String multipartChannel;
    int multipartPos;

}
