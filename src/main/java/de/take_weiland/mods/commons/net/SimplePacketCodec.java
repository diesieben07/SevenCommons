package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
final class SimplePacketCodec {

    private final String channel;
    private final Function<? super MCDataInput, ? extends BasePacket>[] constructors;
    private final ImmutableMap<Class<? extends BasePacket>, HandlerIDPair> handlers;

    SimplePacketCodec(String channel, Function<? super MCDataInput, ? extends BasePacket>[] constructors, ImmutableMap<Class<? extends BasePacket>, HandlerIDPair> handlers) {
        this.channel = channel;
        this.constructors = constructors;
        this.handlers = handlers;
    }


    public byte[] encode(BasePacket packet) {
        MCDataOutput out = prepareOut(packet);
        packet.writeTo(out);
        // remove this copy in 1.8 by wrapping with a ByteBuf
        // when the vanilla packet properly checks for limits on ByteBufs
        return out.toByteArray();
    }

    @Override
    public byte[] encodeToPlayer(BasePacket packet, EntityPlayerMP player) {
        MCDataOutput out = prepareOut(packet);
        packet.writeToPlayer(out, player);
        return out.toByteArray();
    }

    @NotNull
    private MCDataOutput prepareOut(BasePacket packet) {
        MCDataOutput out = new MCDataOutputImpl(packet.expectedSize() + 1);
        // don't need to check for null, packet comes through PacketToChannelMap
        out.writeByte(handlers.get(packet.getClass()).id);
        return out;
    }

    @Override
    public Packet decode(MCDataInput in) {
        byte id = in[0];
        Function<? super MCDataInput, ? extends Packet> cstr = constructors[UnsignedBytes.toInt(id)];
        if (cstr == null) {
            throw new ProtocolException("Unknown PacketID " + id);
        }
        return cstr.apply(new MCDataInputImpl(in, 1, in.length - 1));
    }

    @Override
    public void handle(Packet packet, EntityPlayer player) {
        BiConsumer<? super Packet, ? super EntityPlayer> handler = handlers.get(packet.getClass()).handler;
        (player.worldObj.isRemote ? Scheduler.client() : Scheduler.server()).execute(() -> handler.accept(packet, player));
    }

    @Override
    public String channel() {
        return channel;
    }

}
