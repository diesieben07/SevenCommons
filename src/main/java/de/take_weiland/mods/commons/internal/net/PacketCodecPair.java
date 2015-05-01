package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.PacketCodec;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */

public final class PacketCodecPair<P> {

    private final P packet;
    private final PacketCodec<P> codec;

    public PacketCodecPair(P packet, PacketCodec<P> codec) {
        this.packet = packet;
        this.codec = codec;
    }

    Packet makeClientboundMCPacket() {
        return new S3FPacketCustomPayload(codec.channel(), codec.encode(packet));
    }

    Packet makeServerboundMCPacket() {
        return new C17PacketCustomPayload(codec.channel(), codec.encode(packet));
    }

    void handle(EntityPlayer player) {
        codec.handle(packet, player);
    }
}
