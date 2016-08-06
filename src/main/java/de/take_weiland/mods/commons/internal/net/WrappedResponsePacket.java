package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.internal.net.InternalPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.network.NetworkManager;

/**
 * <p>Wrapper around a response "R" when not on a local channel</p>
 */
public final class WrappedResponsePacket<R extends Packet.Response> implements InternalPacket {

    private final R      response;
    private final int    packetId;
    private final int    uniqueId;
    private final String channel;

    WrappedResponsePacket(R response, int packetId, int uniqueId, String channel) {
        this.response = response;
        this.packetId = packetId;
        this.uniqueId = uniqueId;
        this.channel = channel;
    }

    @Override
    public String _sc$internal$channel() {
        return channel;
    }

    @Override
    public int _sc$internal$expectedSize() {
        return response.expectedSize() + 2;
    }

    @Override
    public void _sc$internal$writeTo(MCDataOutput out) throws Exception {
        out.writeByte(packetId);
        out.writeByte(uniqueId | ResponseSupport.IS_RESPONSE);
        response.writeTo(out);
    }

    @Override
    public void _sc$internal$receiveDirect(byte side, NetworkManager manager) {
        NetworkImpl.LOGGER.warn("Channel {} changed from not-local to local, this should be impossible.", channel);
        ResponseSupport.unregister(uniqueId).complete(response);
    }

    @Override
    public String toString() {
        return String.format("Wrapped response packet (packet=%s, packetID=%s, responseId=%s, channel=%s", response, packetId, uniqueId, channel);
    }
}
