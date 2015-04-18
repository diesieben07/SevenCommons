package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.common.network.NetworkRegistry;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class ChannelBuilderImpl implements NetworkChannelBuilder {

    private BitSet freeIDs = new BitSet(256);
    private final String channel;
    private final TObjectByteHashMap<Class<? extends Packet>> packetToId = new TObjectByteHashMap<>();
    private final TByteObjectHashMap<PacketInfo> idToInfo = new TByteObjectHashMap<>();

    ChannelBuilderImpl(String channel) {
        this.channel = channel;
    }

    @Override
    public <P extends Packet, H extends PacketHandler<P>> NetworkChannelBuilder register(PacketConstructor<? extends P> packet, H handler) {
        return register(freeIDs.nextClearBit(0), packet, handler);
    }

    @Override
    public <P extends Packet, H extends PacketHandler<P>> NetworkChannelBuilder register(int id, PacketConstructor<? extends P> packet, H handler) {
        checkNotDone();
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        checkArgument(!freeIDs.get(id), "ID already taken");

        freeIDs.set(id);
        packetToId.put(packet.getPacketClass(), (byte) id);
        idToInfo.put((byte) id, new PacketInfo<>(packet, handler));

        return this;
    }

    @Override
    public NetworkChannel build() {
        checkNotDone();
        packetToId.compact();
        idToInfo.compact();
        freeIDs = null;
        NetworkChannelImpl handler = new NetworkChannelImpl(packetToId, idToInfo);
        NetworkRegistry.INSTANCE.newChannel(channel, handler);
        return handler;
    }

    private void checkNotDone() {
        checkState(freeIDs != null, "Already built");
    }

}
