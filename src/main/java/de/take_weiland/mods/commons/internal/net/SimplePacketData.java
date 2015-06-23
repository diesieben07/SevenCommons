package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.RawPacket;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * @author diesieben07
 */
public final class SimplePacketData {

    final String channel;
    final int packetID;
    final BiConsumer<BasePacket, EntityPlayer> handler;

    public SimplePacketData(String channel, int packetID, BiConsumer<BasePacket, EntityPlayer> handler) {
        this.channel = channel;
        this.packetID = packetID;
        this.handler = handler;
    }

    MCDataOutput prepareOut(BasePacket packet) {
        MCDataOutput out = Network.newOutput(packet.expectedSize() + 1);
        out.writeByte(packetID);
        return out;
    }

    public void sendToServer(Packet packet) {
        Network.sendToServer(new RawPacket.UsingCustomPayload() {
            @Override
            public void handle(EntityPlayer player) {
                handler.accept(packet, player);
            }

            @Override
            public String channel() {
                return channel;
            }

            @Override
            public byte[] write() {
                MCDataOutput out = prepareOut(packet);
                packet.writeTo(out);
                return out.toByteArray();
            }
        });
    }
}
