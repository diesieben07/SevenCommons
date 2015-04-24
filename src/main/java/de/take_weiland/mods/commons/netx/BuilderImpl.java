package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.hash.TByteObjectHashMap;
import io.netty.buffer.ByteBuf;

import java.util.BitSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class BuilderImpl<P> implements NetworkChannelBuilder<P> {

    private final String channel;
    private final Function<? super P, ? extends ByteBuf> encoder;


    private final BitSet freeIdsServer = new BitSet();
    private final BitSet freeIdsClient = new BitSet();
    private TByteObjectHashMap<Function<? super ByteBuf, ? extends P>> clientHandlers = new TByteObjectHashMap<>();
    private TByteObjectHashMap<Function<? super ByteBuf, ? extends P>> serverHandlers = new TByteObjectHashMap<>();

    BuilderImpl(String channel, Function<? super P, ? extends ByteBuf> encoder) {
        this.channel = channel;
        this.encoder = encoder;
    }

    @Override
    public NetworkChannelBuilder<P> register(Side side, Function<? super ByteBuf, ? extends P> decoder) {
        return register(side, getFreeIds(side).nextClearBit(0), decoder);
    }

    @Override
    public NetworkChannelBuilder<P> register(Side side, int id, Function<? super ByteBuf, ? extends P> decoder) {
        checkNotBuilt();
        BitSet freeIds = getFreeIds(side);
        TByteObjectHashMap<Function<? super ByteBuf, ? extends P>> handlers = side.isClient() ? clientHandlers : serverHandlers;

        checkArgument(id >= 0 && id <= 255, "ID %s out of range, must be 0-255", id);
        checkState(!freeIds.get(id), "ID %s already taken", id);

        freeIds.set(id);
        handlers.put((byte) id, decoder);
        return this;
    }

    private BitSet getFreeIds(Side side) {
        return side.isClient() ? freeIdsClient : freeIdsServer;
    }

    @Override
    public void build() {
        checkNotBuilt();
        clientHandlers.compact();
        serverHandlers.compact();
        NetworkImpl.register(channel, clientHandlers, serverHandlers);

        clientHandlers = serverHandlers = null;
    }

    private void checkNotBuilt() {
        checkState(clientHandlers != null, "Already built!");
    }
}
