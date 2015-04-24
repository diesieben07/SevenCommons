package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * @author diesieben07
 */
public class Network {

    public static final int DEFAULT_EXPECTED_SIZE = 32;

    @Nonnull
    public static <P> NetworkChannelBuilder<P> newChannel(String channel, BiConsumer<? super P, ? super ByteBuf> encoder, ToIntFunction<? super P> idProvider) {
        return newChannel(channel, encoder, idProvider, p -> DEFAULT_EXPECTED_SIZE);
    }

    @Nonnull
    public static <P> NetworkChannelBuilder<P> newChannel(String channel, BiConsumer<? super P, ? super ByteBuf> encoder, ToIntFunction<? super P> idProvider, ToIntFunction<? super P> sizeEstimate) {
        return newChannel(channel, p -> {
            ByteBuf buf = Unpooled.buffer(sizeEstimate.applyAsInt(p) + 1);
            buf.writeByte(idProvider.applyAsInt(p));
            encoder.accept(p, buf);
            return buf;
        });
    }

    public static NetworkChannelBuilder<Packet> newSimpleChannel(String channel) {
        return newChannel(channel, packet -> {
            ByteBuf buf = Unpooled.buffer(packet.expectedSize() + 1);
            buf.writeByte(idFor(packet));
            packet.writeTo(buf);
            return buf;
        });
    }

    private static <P> NetworkChannelBuilder<P> newChannel(String channel, Function<? super P, ? extends ByteBuf> completeEncoder) {
        return new BuilderImpl<>(channel, completeEncoder);
    }

    private static byte idFor(Packet packet) {
        return 0; // TODO
    }

    public static void main(String[] args) {
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
