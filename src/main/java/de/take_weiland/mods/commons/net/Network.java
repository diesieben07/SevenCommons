package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public class Network {

    public static final int DEFAULT_EXPECTED_SIZE = 32;

    public static <P> NetworkChannel<P> newChannel(String channel,
                                      Function<? super P, ? extends ByteBuf> encoder,
                                      Function<? super ByteBuf, ? extends P> decoder,
                                      BiConsumer<? super P, ? super EntityPlayer> handler) {
        return NetworkImpl.register(channel, encoder, decoder, handler);
    }

    public static SimpleChannelBuilder newSimpleChannel(String channel) {
        return new SimpleChannelBuilderImpl(channel);
    }

    public static void main(String[] args) {
        newSimpleChannel("helloWorld")
                .register(0, MyPacket::new, MyPacket::handle)
                .register(1, MyPacket::new, MyPacket::handle);
    }

    private static class MyPacket implements Packet {

        private final String s;

        MyPacket(String s) {
            this.s = s;
        }

        MyPacket(ByteBuf buf) {
            this.s = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void writeTo(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, s);
        }

        void handle(EntityPlayer player, Side side) {
            System.out.println(s);
        }
    }
}
