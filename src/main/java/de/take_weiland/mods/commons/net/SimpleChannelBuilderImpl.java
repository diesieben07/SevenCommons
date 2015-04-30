package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private TByteObjectHashMap<Function<? super ByteBuf, ? extends Packet>> constructors = new TByteObjectHashMap<>();
    private Map<Class<? extends Packet>, BiConsumer<?, ? super EntityPlayer>> handlers = new HashMap<>();

    SimpleChannelBuilderImpl(String channel) {
        this.channel = channel;
    }

    @Override
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        checkArgument(!constructors.containsKey((byte) id), "ID already taken");

        Class<? extends Packet> packetClass = constructor.getPacketClass();
        checkArgument(!handlers.containsKey(packetClass), "Duplicate packet class");

        constructors.put((byte) id, constructor);
        handlers.put(packetClass, handler);

        return this;
    }

    @Override
    public void build() {
        checkNotBuilt();
        TByteObjectHashMap<Function<? super ByteBuf, ? extends Packet>> constructors = this.constructors;
        constructors.compact();

        Map<Class<? extends Packet>, BiConsumer<?, ? super EntityPlayer>> handlers = ImmutableMap.copyOf(this.handlers);

        this.constructors = null;
        this.handlers = null;

        // these lambdas only capture local vars, therefor the builder can be gc'd
        Network.<Packet>newChannel(channel,
                // encoder
                (packet) -> {
                    ByteBuf buf = Unpooled.buffer(packet.expectedSize() + 1);
                    buf.writeByte(0); // TODO
                    packet.writeTo(buf);
                    return buf;
                },
                // decoder
                buf -> {
                    byte id = buf.readByte();
                    return constructors.get(id).apply(buf);
                },
                // handler
                (packet, player) -> {
                    // this is checked before, we only fill the map correctly
                    //noinspection unchecked
                    ((BiConsumer<Packet, EntityPlayer>) handlers.get(packet.getClass())).accept(packet, player);
                });
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

}
