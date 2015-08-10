package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Wrapper around a response "R".</p>
 */
final class ResponseWrapper<R extends Packet.Response> implements BaseNettyPacket {

    private final R response;
    private final int id;
    private final int uniqueID;
    private final String channel;

    ResponseWrapper(R response, int id, int uniqueID, String channel) {
        this.response = response;
        this.id = id;
        this.uniqueID = uniqueID;
        this.channel = channel;
    }

    @Override
    public byte[] _sc$encode() {
        MCDataOutput out = Network.newOutput(response.expectedSize() + 5);
        out.writeByte(id);
        out.writeInt(ResponseSupport.toResponse(uniqueID));
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
}
