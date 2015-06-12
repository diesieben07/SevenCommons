package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.PacketCodec;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public final class ResponseCodec implements PacketCodec<PacketResponsePair<?>> {

    @Override
    public byte[] encode(PacketResponsePair<?> packet) {
        MCDataOutput out = Network.newOutput();
        packet.write(out);
        return out.toByteArray();
    }

    @Override
    public PacketResponsePair<?> decode(MCDataInput in) {
        MCDataInput in = Network.newInput(in);
        String channel = in.readString();
        int id = in.readUnsignedByte();
        PacketCodec<?> codec = NetworkImpl.channels.get(channel);
        codec.decodeAndHandle(in, );
    }

    @Override
    public void handle(PacketResponsePair<?> packet, EntityPlayer player) {

    }

    @Override
    public String channel() {
        return "SC|Response";
    }
}
