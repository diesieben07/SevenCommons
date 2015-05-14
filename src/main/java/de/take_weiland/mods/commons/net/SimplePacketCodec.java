package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
final class SimplePacketCodec implements PacketCodec<Packet> {

    private final String channel;
    private final TByteObjectHashMap<Function<? super MCDataInput, ? extends Packet>> constructors;
    private final ImmutableMap<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers;
    private final TObjectByteHashMap<Class<? extends Packet>> classToIdMap;

    SimplePacketCodec(String channel, TByteObjectHashMap<Function<? super MCDataInput, ? extends Packet>> constructors, ImmutableMap<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers, TObjectByteHashMap<Class<? extends Packet>> classToIdMap) {
        this.channel = channel;
        this.constructors = constructors;
        this.handlers = handlers;
        this.classToIdMap = classToIdMap;
    }

    @Override
    public byte[] encode(Packet packet) {
        MCDataOutputImpl out = new MCDataOutputImpl(packet.expectedSize() + 1);

        /**
         * We don't need to check for contains here, because this codec is only used through
         * PacketToChannelMap, so only valid packets can end up here.
         */
        out.writeByte(classToIdMap.get(packet.getClass()));
        packet.writeTo(out);
        // remove this copy in 1.8 by wrapping with a ByteBuf
        // when the vanilla packet properly checks for limits on ByteBufs
        return out.toByteArray();
    }

    @Override
    public Packet decode(byte[] payload) {
        byte id = payload[0];
        Function<? super MCDataInput, ? extends Packet> cstr = constructors.get(id);
        if (cstr == null) {
            throw new ProtocolException("Unknown PacketID " + id);
        }
        return cstr.apply(new MCDataInputImpl(payload, 1, payload.length - 1));
    }

    @Override
    public void handle(Packet packet, EntityPlayer player) {
        BiConsumer<? super Packet, ? super EntityPlayer> handler = handlers.get(packet.getClass());
        (player.worldObj.isRemote ? Scheduler.client() : Scheduler.server()).execute(() -> handler.accept(packet, player));
    }

    @Override
    public String channel() {
        return channel;
    }
}
