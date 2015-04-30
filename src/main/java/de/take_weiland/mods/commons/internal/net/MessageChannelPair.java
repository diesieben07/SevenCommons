package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

/**
 * @author diesieben07
 */

public final class MessageChannelPair<P> {

    final P message;
    final NetworkChannelImpl<P> channel;

    MessageChannelPair(P message, NetworkChannelImpl<P> channel) {
        this.message = message;
        this.channel = channel;
    }

    net.minecraft.network.Packet makeMcPacket() {
        return new S3FPacketCustomPayload(channel.channel, channel.encoder.apply(message));
    }

    void handle(EntityPlayer player) {
        channel.handler.accept(message, player);
    }
}
