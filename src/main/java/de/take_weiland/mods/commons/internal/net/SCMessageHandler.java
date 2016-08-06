package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.ProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

import static net.minecraftforge.fml.relauncher.FMLLaunchHandler.side;

/**
 * @author diesieben07
 */
public abstract class SCMessageHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ProtocolException) {
            ProtocolException pe = (ProtocolException) cause;
            if (pe.getKickMessage() != null) {
                ctx.attr(NetworkDispatcher.FML_DISPATCHER).get().manager.closeChannel(new TextComponentString(pe.getKickMessage()));
            }
            NetworkImpl.LOGGER.warn("ProtocolException on " + (side().name().toLowerCase()) + "-side netty channel", pe);
        }
        super.exceptionCaught(ctx, cause);
    }

    ByteBuf[] multipartData = new ByteBuf[4];
    String multipartChannel;
    int multipartPos;

}
