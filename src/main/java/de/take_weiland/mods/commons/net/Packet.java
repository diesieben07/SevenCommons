package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.*;
import net.minecraft.network.NetworkManager;

import java.util.concurrent.CompletionStage;

/**
 * <p>A simple interface for implementing custom packets for your Mod. They will be registered using {@link Network#newSimpleChannel(String)}
 * and will be a sent using the vanilla custom payload packets.</p>
 * <p>An instance of {@code Packet} must never carry mutable state that is referenced outside of it unless it is ensured that the state
 * can be accessed from multiple threads concurrently.</p>
 * <p>If a packet is send through the local channel on SSP <i>the same instance</i> will be received on the other side.</p>
 * <p>{@code Packet} extends {@link SimplePacket}, the methods defined there are used to send this.</p>
 *
 * @see SimpleChannelBuilder
 * @see SimplePacket
 *
 * @author diesieben07
 */
public interface Packet extends SimplePacket, PacketBase, InternalPacket, PacketWithData {

    @Override
    @Deprecated
    default void _sc$internal$writeTo(MCDataOutput out) throws Exception {
        out.writeByte(_sc$internal$getData().packetId);
        writeTo(out);
    }

    @Override
    @Deprecated
    default int _sc$internal$expectedSize() {
        return expectedSize() + 1; // packetId
    }

    @Override
    @Deprecated
    default void _sc$internal$receiveDirect(byte side, NetworkManager manager) {
        PacketData data = _sc$internal$getData();
        NetworkImpl.validateSide(data.characteristics, side, this);
        if ((data.characteristics & Network.ASYNC) != 0) {
            //noinspection unchecked,rawtypes
            ((PacketHandler) data.handler).handle(this, NetworkImpl.getPlayer(side, manager));
        } else {
            //noinspection rawtypes
            PacketHandler handler = (PacketHandler) data.handler;
            NetworkImpl.getScheduler(side).execute(() -> {
                //noinspection unchecked
                handler.handle(this, NetworkImpl.getPlayer(side, manager));
                return false;
            });
        }
    }

    @Override
    @Deprecated
    default String _sc$internal$channel() {
        return _sc$internal$getData().channel;
    }

    /**
     * <p>A version of {@code Packet} that has a response. The response class needs to implement {@link de.take_weiland.mods.commons.net.Packet.Response}.</p>
     * <p>The response is supplied in form of a {@link CompletionStage} when an instance of this packet is sent.</p>
     * <p>{@code Packet.WithResponse} extends {@link SimplePacket.WithResponse}, the methods defined there are used to send this packet.</p>
     *
     * @see SimplePacket.WithResponse
     */
    interface WithResponse<R extends Packet.Response> extends SimplePacket.WithResponse<R>, PacketBase, PacketWithData {

        @Override
        default CompletionStage<R> sendTo(NetworkManager manager) {
            AcceptingCompletableFuture<R> future = new AcceptingCompletableFuture<>();
            NetworkImpl.sendPacket(new WrappedPacketWithResponse<>(this, future), manager);
            return future;
        }

    }
    /**
     * <p>The response for a {@code Packet.WithResponse}.</p>
     */
    interface Response extends PacketBase {
    }
}
