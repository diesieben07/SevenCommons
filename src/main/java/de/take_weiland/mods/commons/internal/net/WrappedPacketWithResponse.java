package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.network.NetworkManager;

/**
 * @author diesieben07
 */
public final class WrappedPacketWithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> implements InternalPacket {

    private final P original;
    private final AcceptingCompletableFuture<R> future;

    public WrappedPacketWithResponse(P original, AcceptingCompletableFuture<R> future) {
        this.original = original;
        this.future = future;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void _sc$internal$receiveDirect(byte side, NetworkManager manager) {
        PacketData data = original._sc$internal$getData();
        NetworkImpl.validateSide(data.characteristics, side, original);
        //noinspection unchecked
        PacketHandlerBaseWithResponse<P, R> handler = (PacketHandlerBaseWithResponse<P, R>) data.handler;
        if ((data.characteristics & Network.ASYNC) == 0) {
            NetworkImpl.getScheduler(side).execute(() -> {
                handler._sc$internal$handleInto(original, future, side, manager);
                return false;
            });
        } else {
            handler._sc$internal$handleInto(original, future, side, manager);
        }
    }

    @Override
    public int _sc$internal$expectedSize() {
        return original.expectedSize() + 2;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String _sc$internal$channel() {
        return original._sc$internal$getData().channel;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void _sc$internal$writeTo(MCDataOutput out) throws Exception {
        int uniqueId = ResponseSupport.register(future);
        out.writeByte(original._sc$internal$getData().packetId);
        out.writeByte(uniqueId);

        original.writeTo(out);
    }

    @Override
    public String toString() {
        return String.format("Wrapped packet with response (packet=%s, future=%s)", original, future);
    }
}
