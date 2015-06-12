package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
abstract class PacketResponsePair<R extends Packet> {

    final String channel;
    final int id;
    final R response;

    PacketResponsePair(String channel, int id, R response) {
        this.channel = channel;
        this.id = id;
        this.response = response;
    }

    void write(MCDataOutput out) {
        out.writeString(channel);
        out.writeByte(id);
        response.writeTo(out);
    }

    abstract void handle(EntityPlayer player);

    static final class Sending<R> extends PacketResponsePair<R> {

        private final Packet.WithResponse<R> packet;

        Sending(int id, R response, Packet.WithResponse<R> packet) {
            super(channel, id, response);
            this.packet = packet;
        }

        void write(MCDataOutput out) {
            packet.writeResponse(response, out);
        }

    }

    static final class Receiving<R> extends PacketResponsePair<R> {

        Receiving(MCDataInput in) {
            super(channel, in.readInt(), response);
        }
    }


}
