package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Wrapper around a response "R".</p>
 */
final class WrappedResponsePacket<R extends Packet.Response> implements BaseNettyPacket {

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
    public byte[] _sc$encode() throws Exception {
        MCDataOutput out = Network.newOutput(response.expectedSize() + 5);
        out.writeByte(packetId);
        out.writeInt(ResponseSupport.toResponse(uniqueId));
        response.writeTo(out);
        return out.toByteArray();
    }

    @Override
    public void _sc$handle(EntityPlayer player) {
        // this should never happen
        // if we are on a local, direct connection the response itself should be handling
        // see ResponseNettyVersion
        throw new AssertionError();
    }

    @Override
    public String _sc$channel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("Wrapped response packet (packet=%s, packetID=%s, responseId=%s, channel=%s", response, packetId, uniqueId, channel);
    }
}
