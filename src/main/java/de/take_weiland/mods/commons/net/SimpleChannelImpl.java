package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author diesieben07
 */
final class SimpleChannelImpl extends FMLIndexedMessageToMessageCodec<Packet> {


    @Override
    public void encodeInto(ChannelHandlerContext ctx, Packet msg, ByteBuf target) throws Exception {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, Packet msg) {

    }
}
