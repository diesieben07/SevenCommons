package de.take_weiland.mods.commons.internal.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import java.io.IOException;

/**
 * @author diesieben07
 */
public final class SCMessageHandlerServer extends ChannelInboundHandlerAdapter implements PartTracker {

    private final EntityPlayerMP player;

    public SCMessageHandlerServer(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BaseNettyPacket) {
            ((BaseNettyPacket) msg)._sc$handle(player);
        } else if (!(msg instanceof C17PacketCustomPayload) || !NetworkImpl.handleServerCustomPacket((C17PacketCustomPayload) msg, player, this)) {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("Exception in netty pipeline on server!");
        cause.printStackTrace();
        ctx.fireExceptionCaught(cause);
    }

    private byte[] multipartData;
    private String multipartChannel;
    private int multipartPos;

    @Override
    public void start(String channel, int len) throws IOException {
        if (multipartChannel != null) {
            throw new IOException("New Multipart packet before old was finished");
        }
        multipartChannel = channel;
        multipartData = new byte[len];
        multipartPos = 0;
    }

    @Override
    public void onPart(byte[] part) {
        int actLen = part.length - 1;

        System.arraycopy(part, 1, multipartData, multipartPos, actLen);
        multipartPos += actLen;
    }

    @Override
    public byte[] checkDone() {
        if (multipartPos == multipartData.length) {
            return multipartData;
        } else {
            return null;
        }
    }

    @Override
    public String channel() {
        String channel = multipartChannel;
        multipartData = null;
        multipartChannel = null;
        return channel;
    }
}
