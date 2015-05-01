package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.internal.net.MCDataOutputImpl;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
final class SimplePacketCodec implements PacketCodec<Packet> {

    private final String channel;
    private final TByteObjectHashMap<Function<? super ByteBuf, ? extends Packet>> constructors;
    private final ImmutableMap<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers;
    private final TObjectByteHashMap<Class<? extends Packet>> classToIdMap;

    SimplePacketCodec(String channel, TByteObjectHashMap<Function<? super ByteBuf, ? extends Packet>> constructors, ImmutableMap<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers, TObjectByteHashMap<Class<? extends Packet>> classToIdMap) {
        this.channel = channel;
        this.constructors = constructors;
        this.handlers = handlers;
        this.classToIdMap = classToIdMap;
    }

    @Override
    public ByteBuf encode(Packet packet) {
        ByteBuf buf = Unpooled.buffer(packet.expectedSize() + 1);

        /**
         * We don't need to check for contains here, because this codec is only used through
         * PacketToChannelMap, so only valid packets can end up here.
         */
        buf.writeByte(classToIdMap.get(packet.getClass()));
        packet.writeTo(new MCDataOutputImpl(buf));
        return buf;
    }

    @Override
    public Packet decode(ByteBuf buf) {
        byte id = buf.readByte();
        Function<? super ByteBuf, ? extends Packet> cstr = constructors.get(id);
        if (cstr == null) {
            throw new ProtocolException("Unknown PacketID " + id);
        }
        return cstr.apply(buf);
    }

    @Override
    public void handle(Packet packet, EntityPlayer player) {
        handlers.get(packet.getClass()).accept(packet, player);
    }

    @Override
    public String channel() {
        return channel;
    }
}
